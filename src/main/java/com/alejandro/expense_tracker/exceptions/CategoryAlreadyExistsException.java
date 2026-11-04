package com.alejandro.expense_tracker.exceptions;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException (){
        super(" ya existe la categoria con este nombre y este tipo");
    }
}
