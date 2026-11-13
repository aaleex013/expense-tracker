package com.alejandro.expense_tracker.exceptions;

public class ExpenseOccurrenceAlreadyPaidException extends RuntimeException {
    public ExpenseOccurrenceAlreadyPaidException() {
        super("vencimiento ya pagado");
    }
}
