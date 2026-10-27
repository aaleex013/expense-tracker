package com.alejandro.expense_tracker.auth.dto;

public record RegisterResponse(
        Long id,
        String username,
        String email

) {
}
