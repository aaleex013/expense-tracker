package com.alejandro.expense_tracker.exceptions;

public class InvalidRecurringExpenseScheduleException extends RuntimeException {
    // se recibe el  mensaje porque hay 2 tipos de mensaje
    public InvalidRecurringExpenseScheduleException(String message) {
        super(message);
    }
}
