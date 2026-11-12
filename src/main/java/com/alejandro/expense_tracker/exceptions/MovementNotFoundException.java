package com.alejandro.expense_tracker.exceptions;

public class MovementNotFoundException extends RuntimeException {
    public MovementNotFoundException() {
        super("no se ha encontrado el movimiento");
    }
}
