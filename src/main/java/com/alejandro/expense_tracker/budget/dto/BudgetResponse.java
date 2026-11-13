package com.alejandro.expense_tracker.budget.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        YearMonth month,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BigDecimal remainingAmount,
        Instant createdAt
) {
}
