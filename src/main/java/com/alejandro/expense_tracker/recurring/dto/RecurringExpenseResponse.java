package com.alejandro.expense_tracker.recurring.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record RecurringExpenseResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String name,
        BigDecimal amount,
        RecurringExpenseFrequency frequency,
        Short dayOfMonth,
        Short monthOfYear,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        Instant createdAt
) {
}
