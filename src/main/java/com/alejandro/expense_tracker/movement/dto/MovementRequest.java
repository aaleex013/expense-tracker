package com.alejandro.expense_tracker.movement.dto;

import com.alejandro.expense_tracker.movement.MovementType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MovementRequest(
        @NotNull(message = "La categoría es obligatoria")
        @Positive(message = "El identificador de la categoría debe ser positivo")
        Long categoryId,
        @NotNull(message = "El tipo es obligatorio")
        MovementType type,

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.50",
                message = "El importe debe ser al menos 0.50 centimos")
        @Digits(integer = 10, fraction = 2,
                message = "El importe admite hasta 10 enteros y 2 decimales")
        BigDecimal amount,
        @NotNull(message = "La fecha es obligatoria")
        LocalDate movementDate,
        @Size(max = 255,
                message = "La descripción no puede superar los 255 caracteres")
        String descripcion

) {
}
