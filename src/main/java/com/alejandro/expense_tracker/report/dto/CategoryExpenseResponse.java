package com.alejandro.expense_tracker.report.dto;

import java.math.BigDecimal;

// Total gastado en una categoría durante el periodo consultado
public record CategoryExpenseResponse(
        Long categoryId,
        String categoryName,
        BigDecimal totalAmount
) {
}
