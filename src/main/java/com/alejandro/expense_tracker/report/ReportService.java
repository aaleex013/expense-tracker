package com.alejandro.expense_tracker.report;

import com.alejandro.expense_tracker.movement.MovementRepository;
import com.alejandro.expense_tracker.movement.MovementType;
import com.alejandro.expense_tracker.report.dto.CategoryExpenseResponse;
import com.alejandro.expense_tracker.report.dto.MonthlyReportResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ReportService {

    private final MovementRepository movementRepository;

    // Spring proporciona el repositorio que consultará los movimientos.
    public ReportService(MovementRepository movementRepository) {
        this.movementRepository = movementRepository;
    }

    @Transactional(
            readOnly = true,
            isolation = Isolation.REPEATABLE_READ
    )
    public MonthlyReportResponse getMonthlyReport(
            Long userId,
            YearMonth month
    ) {
        // Incluimos el primer día del mes y excluimos el del siguiente.
        LocalDate fromDate = month.atDay(1);
        LocalDate toDateExclusive = month.plusMonths(1).atDay(1);

        // Calculamos todos los ingresos del usuario durante ese mes.
        BigDecimal totalIncome = movementRepository.sumAmountForPeriod(
                userId,
                MovementType.INCOME,
                fromDate,
                toDateExclusive
        );

        // Calculamos todos sus gastos del mismo periodo.
        BigDecimal totalExpenses = movementRepository.sumAmountForPeriod(
                userId,
                MovementType.EXPENSE,
                fromDate,
                toDateExclusive
        );

        // Un resultado negativo indica que gastó más de lo que ingresó.
        BigDecimal balance = totalIncome.subtract(totalExpenses);

        // Obtenemos los gastos agrupados y ordenados por categoría.
        List<CategoryExpenseResponse> expensesByCategory =
                movementRepository.findExpenseTotalsByCategory(
                        userId,
                        MovementType.EXPENSE,
                        fromDate,
                        toDateExclusive
                );

        // Reunimos los resultados en una única respuesta.
        return new MonthlyReportResponse(
                month,
                totalIncome,
                totalExpenses,
                balance,
                expensesByCategory
        );
    }
}