package com.alejandro.expense_tracker.exceptions;

public class InvalidMovementTypeException extends RuntimeException {
    public InvalidMovementTypeException() {
        super("Invalid movement type");
    }
}
