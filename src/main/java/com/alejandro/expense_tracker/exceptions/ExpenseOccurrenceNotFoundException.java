package com.alejandro.expense_tracker.exceptions;

public class ExpenseOccurrenceNotFoundException extends RuntimeException {
    public ExpenseOccurrenceNotFoundException( ) {
        super("vencimiento no encontrado");
    }
}
