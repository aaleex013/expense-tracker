package com.alejandro.expense_tracker.exceptions;

public class InvalidBudgetCategoryException extends RuntimeException {
    public InvalidBudgetCategoryException( ) {
        super("Solo se pueden crear presupuestos para categorías de gasto");
    }
}
