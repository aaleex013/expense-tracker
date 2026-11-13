package com.alejandro.expense_tracker.exceptions;

public class BudgetAlreadyExistsException extends RuntimeException {
    public BudgetAlreadyExistsException( ) {
        super("El presupuesto ya existe para esta categoria");
    }
}
