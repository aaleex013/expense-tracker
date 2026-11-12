package com.alejandro.expense_tracker.exceptions;

public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException() {
        super("La fecha inicial no puede ser posterior a la fecha final");
    }
}
