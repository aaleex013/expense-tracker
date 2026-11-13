package com.alejandro.expense_tracker.recurring.dto;

import jakarta.validation.constraints.NotNull;

public record RecurringExpenseActiveRequest (
        @NotNull(message = "El estado activo es obligatorio")
        Boolean active
)
{
}
