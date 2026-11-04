package com.alejandro.expense_tracker.category.dto;


import com.alejandro.expense_tracker.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 100, message = "el nombre no puede superar los 100 caracteres")

        @NotNull(message = "el type es obligatorio")
        String name,
        CategoryType type
) {
}
