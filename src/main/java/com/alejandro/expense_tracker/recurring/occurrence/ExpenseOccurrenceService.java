package com.alejandro.expense_tracker.recurring.occurrence;

import com.alejandro.expense_tracker.exceptions.ExpenseOccurrenceAlreadyPaidException;
import com.alejandro.expense_tracker.exceptions.ExpenseOccurrenceNotFoundException;
import com.alejandro.expense_tracker.movement.Movement;
import com.alejandro.expense_tracker.movement.MovementRepository;
import com.alejandro.expense_tracker.movement.MovementType;
import com.alejandro.expense_tracker.recurring.RecurringExpense;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseFrequency;
import com.alejandro.expense_tracker.recurring.RecurringExpenseRepository;
import com.alejandro.expense_tracker.recurring.occurrence.dto.ConfirmExpensePaymentRequest;
import com.alejandro.expense_tracker.recurring.occurrence.dto.ExpenseOccurrenceResponse;
import com.alejandro.expense_tracker.recurring.occurrence.dto.GenerateOccurrencesRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpenseOccurrenceService {

    private final ExpenseOccurrenceRepository occurrenceRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final MovementRepository movementRepository;

    public ExpenseOccurrenceService(
            ExpenseOccurrenceRepository occurrenceRepository,
            RecurringExpenseRepository recurringExpenseRepository,
            MovementRepository movementRepository
    ) {
        this.occurrenceRepository = occurrenceRepository;
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public List<ExpenseOccurrenceResponse> generateForMonth(
            Long userId,
            GenerateOccurrencesRequest request
    ) {
        YearMonth month = request.month();

        // Solo generamos vencimientos para configuraciones activas del usuario.
        // El bloqueo evita que dos generaciones creen lo mismo simultáneamente.
        List<RecurringExpense> recurringExpenses =
                recurringExpenseRepository
                        .findAllByUser_IdAndActiveTrueOrderByIdAsc(userId);

        List<ExpenseOccurrence> newOccurrences = new ArrayList<>();

        for (RecurringExpense recurringExpense : recurringExpenses) {

            // Un gasto anual solo corresponde al mes que tiene configurado.
            if (recurringExpense.getFrequency()
                    == RecurringExpenseFrequency.YEARLY
                    && recurringExpense.getMonthOfYear().intValue()
                    != month.getMonthValue()) {
                continue;
            }

            // Si el día solicitado no existe, usamos el último día del mes.
            int day = Math.min(
                    recurringExpense.getDayOfMonth().intValue(),
                    month.lengthOfMonth()
            );

            LocalDate expirationDate = month.atDay(day);

            // La fecha inicial está incluida.
            if (expirationDate.isBefore(recurringExpense.getStartDate())) {
                continue;
            }

            // La fecha final también está incluida, cuando existe.
            LocalDate endDate = recurringExpense.getEndDate();

            if (endDate != null && expirationDate.isAfter(endDate)) {
                continue;
            }

            // Si ya existe, lo conservamos y no generamos otro.
            boolean alreadyExists = occurrenceRepository
                    .existsByRecurringExpense_IdAndExpirationDate(
                            recurringExpense.getId(),
                            expirationDate
                    );

            if (alreadyExists) {
                continue;
            }

            newOccurrences.add(
                    new ExpenseOccurrence(
                            recurringExpense,
                            expirationDate
                    )
            );
        }

        List<ExpenseOccurrence> savedOccurrences =
                occurrenceRepository.saveAll(newOccurrences);

        // Devolvemos únicamente los vencimientos creados en esta petición.
        return savedOccurrences.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseOccurrenceResponse> findAll(
            Long userId,
            YearMonth month
    ) {
        LocalDate fromDate = month.atDay(1);
        LocalDate toDateExclusive = month.plusMonths(1).atDay(1);

        return occurrenceRepository
                .findForPeriod(userId, fromDate, toDateExclusive)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ExpenseOccurrenceResponse confirmPayment(
            Long userId,
            Long occurrenceId,
            ConfirmExpensePaymentRequest request
    ) {
        // La búsqueda comprueba al propietario y bloquea el vencimiento.
        ExpenseOccurrence occurrence = occurrenceRepository
                .findByIdAndUser_Id(occurrenceId, userId)
                .orElseThrow(ExpenseOccurrenceNotFoundException::new);

        if (occurrence.getStatus() == ExpenseOccurrenceStatus.PAID) {
            throw new ExpenseOccurrenceAlreadyPaidException();
        }

        // Creamos el gasto usando los datos guardados en el vencimiento.
        Movement movement = new Movement(
                occurrence.getUser(),
                occurrence.getCategory(),
                MovementType.EXPENSE,
                occurrence.getAmount(),
                request.paymentDate(),
                occurrence.getName()
        );

        Movement savedMovement = movementRepository.save(movement);

        // Vinculamos el movimiento y marcamos el vencimiento como pagado.
        occurrence.confirmPayment(savedMovement);

        // Ambas operaciones se confirman juntas al terminar la transacción.
        return toResponse(occurrence);
    }

    // Preparamos los datos que devolverá la API.
    private ExpenseOccurrenceResponse toResponse(
            ExpenseOccurrence occurrence
    ) {
        Long movementId = occurrence.getMovement() == null
                ? null
                : occurrence.getMovement().getId();

        return new ExpenseOccurrenceResponse(
                occurrence.getId(),
                occurrence.getRecurringExpense().getId(),
                occurrence.getName(),
                occurrence.getCategory().getId(),
                occurrence.getCategory().getName(),
                occurrence.getAmount(),
                occurrence.getExpirationDate(),
                occurrence.getStatus(),
                movementId,
                occurrence.getPaidAt(),
                occurrence.getCreatedAt()
        );
    }
}