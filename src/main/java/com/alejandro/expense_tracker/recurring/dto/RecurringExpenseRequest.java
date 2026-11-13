package com.alejandro.expense_tracker.recurring.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecurringExpenseRequest(
        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "El identificador de la categoría debe ser positivo")
        Long categoryId,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100,
                message = "El nombre no puede superar los 100 caracteres")
        String name,

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01",
                message = "El importe debe ser al menos 0.01")
        @Digits(integer = 10, fraction = 2,
                message = "El importe admite hasta 10 enteros y 2 decimales")
        BigDecimal amount,

        @NotNull(message = "La frecuencia es obligatoria")
        RecurringExpenseFrequency frequency,

        @NotNull(message = "El día del mes es obligatorio")
        @Min(value = 1, message = "El día del mes debe ser al menos 1")
        @Max(value = 31, message = "El día del mes no puede superar 31")
        Short dayOfMonth,

        // Puede ser null para los mensuales
        // El servicio comprobará que sea obligatorio para los anuales
        @Min(value = 1, message = "El mes debe ser al menos 1")
        @Max(value = 12, message = "El mes no puede superar 12")
        Short monthOfYear,

        @NotNull(message = "La fecha inicial es obligatoria")
        LocalDate startDate,

        // Es opcional: null significa que no hay fecha final.
        LocalDate endDate

) {
}
