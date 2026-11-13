package com.alejandro.expense_tracker.exceptions;

public class InvalidRecurringExpenseCategoryException extends RuntimeException {
    public InvalidRecurringExpenseCategoryException( ) {
        super("la categoria tiene que ser Gasto");
    }
}
