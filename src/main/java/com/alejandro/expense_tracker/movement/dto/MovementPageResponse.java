package com.alejandro.expense_tracker.movement.dto;

import java.util.List;

public record MovementPageResponse(
        List<MovementResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
}
