package com.alejandro.expense_tracker.recurring.occurrence.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Fecha que tendrá el movimiento de gasto
public record ConfirmExpensePaymentRequest(
        @NotNull(message = "La fecha de pago es obligatoria")
        LocalDate paymentDate
) {
}
