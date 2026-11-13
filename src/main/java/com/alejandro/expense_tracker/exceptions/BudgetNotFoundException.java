package com.alejandro.expense_tracker.exceptions;

public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException() {
        super("presupuesto no encontrado");
    }
}
