package com.alejandro.expense_tracker.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType
) {
}
