package com.alejandro.expense_tracker.report.dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

public record MonthlyReportResponse(
        YearMonth month,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal balance,
        List<CategoryExpenseResponse> expensesByCategory
) {
}
