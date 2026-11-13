package com.alejandro.expense_tracker.exceptions;

public class RecurringExpenseNotFoundException extends RuntimeException {
    public RecurringExpenseNotFoundException() {
        super("no existe ningún gasto recurrente");
    }
}
