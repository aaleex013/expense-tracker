package com.alejandro.expense_tracker.recurring.occurrence.dto;

import jakarta.validation.constraints.NotNull;

import java.time.YearMonth;

// Mes para el que queremos generar vencimientos
public record GenerateOccurrencesRequest(
        @NotNull(message = "El mes es obligatorio")
        YearMonth month
) {
}
