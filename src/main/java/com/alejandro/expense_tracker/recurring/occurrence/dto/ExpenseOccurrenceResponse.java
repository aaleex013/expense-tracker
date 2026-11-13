package com.alejandro.expense_tracker.recurring.occurrence.dto;

import com.alejandro.expense_tracker.recurring.occurrence.ExpenseOccurrenceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

// Información del vencimiento y pago que recibe
public record ExpenseOccurrenceResponse(
        Long id,
        Long recurringExpenseId,
        String name,
        Long categoryId,
        String categoryName,
        BigDecimal amount,
        LocalDate expirationDate,
        ExpenseOccurrenceStatus status,
        Long movementId,
        Instant paidAt,
        Instant createdAt
) {
}
