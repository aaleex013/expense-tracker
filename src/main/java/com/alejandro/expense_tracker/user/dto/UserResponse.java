package com.alejandro.expense_tracker.user.dto;

public record UserResponse(
        Long id,
        String username,
        String email
) {
}
