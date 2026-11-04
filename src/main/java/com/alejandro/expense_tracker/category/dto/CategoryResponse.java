package com.alejandro.expense_tracker.category.dto;

import com.alejandro.expense_tracker.category.CategoryType;

public record CategoryResponse(
        Long id,
        String name,
        CategoryType type
) {
}
