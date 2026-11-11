package com.alejandro.expense_tracker.movement.dto;

import com.alejandro.expense_tracker.movement.MovementType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record MovementResponse(
        Long id,
        Long categoryId,
        String categoryName,
        MovementType type,
        BigDecimal amount,
        LocalDate movementDate,
        String descripcion,
        Instant createdAt

) {
}
