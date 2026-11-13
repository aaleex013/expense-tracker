package com.alejandro.expense_tracker.recurring;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.category.CategoryRepository;
import com.alejandro.expense_tracker.category.CategoryType;
import com.alejandro.expense_tracker.exceptions.*;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseActiveRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseFrequency;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecurringExpenseService {
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final CategoryRepository categoryRepository;

    public RecurringExpenseService(
            RecurringExpenseRepository recurringExpenseRepository,
            CategoryRepository categoryRepository
    ) {
        this.recurringExpenseRepository = recurringExpenseRepository;
        this.categoryRepository = categoryRepository;
    }
    @Transactional
    public RecurringExpenseResponse create(
            Long userId,
            RecurringExpenseRequest request
    ) {
        validateDates(request.startDate(), request.endDate());
        validateSchedule(request);

        Category category = findExpenseCategory(
                userId,
                request.categoryId()
        );

        RecurringExpense recurringExpense = new RecurringExpense(
                category.getUser(),
                category,
                request.name().strip(),
                request.amount(),
                request.frequency(),
                request.dayOfMonth(),
                request.monthOfYear(),
                request.startDate(),
                request.endDate()
        );

        RecurringExpense savedExpense =
                recurringExpenseRepository.save(recurringExpense);

        return toResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<RecurringExpenseResponse> findAll(
            Long userId,
            boolean activeOnly
    ) {
        // Elegimos si consultar todos o únicamente los activos.
        List<RecurringExpense> expenses;

        if (activeOnly) {
            expenses = recurringExpenseRepository
                    .findAllByUser_IdAndActiveTrueOrderByNameAscIdAsc(userId);
        } else {
            expenses = recurringExpenseRepository
                    .findAllByUser_IdOrderByNameAscIdAsc(userId);
        }

        return expenses.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public RecurringExpenseResponse update(
            Long userId,
            Long recurringExpenseId,
            RecurringExpenseRequest request
    ) {
        // Comprobamos el propietario antes de modificar la configuración.
        RecurringExpense recurringExpense =
                findOwnedExpense(userId, recurringExpenseId);

        validateDates(request.startDate(), request.endDate());
        validateSchedule(request);

        Category category = findExpenseCategory(
                userId,
                request.categoryId()
        );

        recurringExpense.updateDetails(
                category,
                request.name().strip(),
                request.amount(),
                request.frequency(),
                request.dayOfMonth(),
                request.monthOfYear(),
                request.startDate(),
                request.endDate()
        );

        return toResponse(recurringExpense);
    }

    @Transactional
    public void delete(Long userId, Long recurringExpenseId) {
        RecurringExpense recurringExpense =
                findOwnedExpense(userId, recurringExpenseId);

        // Esta operación elimina el registro de la base de datos.
        recurringExpenseRepository.delete(recurringExpense);
    }

    // Centralizamos la búsqueda para no repetir la comprobación del propietario.
    private RecurringExpense findOwnedExpense(
            Long userId,
            Long recurringExpenseId
    ) {
        return recurringExpenseRepository
                .findByIdAndUser_Id(recurringExpenseId, userId)
                .orElseThrow(RecurringExpenseNotFoundException::new);
    }

    private Category findExpenseCategory(Long userId, Long categoryId) {
        Category category = categoryRepository
                .findByIdAndUser_Id(categoryId, userId)
                .orElseThrow(CategoryNotFoundException::new);

        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidRecurringExpenseCategoryException();
        }

        return category;
    }

    private void validateDates( LocalDate startDate, LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidDateRangeException();
        }
    }
    private void validateSchedule(RecurringExpenseRequest request) {
        // Un gasto mensual se repite todos los meses: no debe elegir uno concreto.
        if (request.frequency() == RecurringExpenseFrequency.MONTHLY
                && request.monthOfYear() != null) {
            throw new InvalidRecurringExpenseScheduleException(
                    "Los gastos mensuales no deben indicar un mes del año"
            );
        }

        // Un gasto anual necesita indicar en qué mes corresponde pagar.
        if (request.frequency() == RecurringExpenseFrequency.YEARLY
                && request.monthOfYear() == null) {
            throw new InvalidRecurringExpenseScheduleException(
                    "Los gastos anuales deben indicar un mes del año"
            );
        }
    }

    @Transactional
    public RecurringExpenseResponse updateActive(
            Long userId,
            Long recurringExpenseId,
            RecurringExpenseActiveRequest request
    ) {
        // Solo podemos cambiar el estado de un gasto recurrente propio.
        RecurringExpense recurringExpense =
                findOwnedExpense(userId, recurringExpenseId);

        recurringExpense.updateActive(request.active());

        // Hibérnate guardará el nuevo estado al confirmar la transacción.
        return toResponse(recurringExpense);
    }

    // Convertimos la entidad al formato que devolverá la API
    private RecurringExpenseResponse toResponse(
            RecurringExpense recurringExpense
    ) {
        return new RecurringExpenseResponse(
                recurringExpense.getId(),
                recurringExpense.getCategory().getId(),
                recurringExpense.getCategory().getName(),
                recurringExpense.getName(),
                recurringExpense.getAmount(),
                recurringExpense.getFrequency(),
                recurringExpense.getDayOfMonth(),
                recurringExpense.getMonthOfYear(),
                recurringExpense.getStartDate(),
                recurringExpense.getEndDate(),
                recurringExpense.isActive(),
                recurringExpense.getCreatedAt()
        );
    }
}
