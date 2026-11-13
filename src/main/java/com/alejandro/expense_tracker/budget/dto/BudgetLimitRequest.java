package com.alejandro.expense_tracker.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetLimitRequest(
        @NotNull(message = "El límite es obligatorio")
        @DecimalMin(value = "0.01",
                message = "El límite debe ser al menos 0.01")
        @Digits(integer = 10, fraction = 2,
                message = "El límite admite hasta 10 enteros y 2 decimales")
        BigDecimal limitAmount
) {
}
