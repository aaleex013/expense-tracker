package com.alejandro.expense_tracker.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException() {
        super("Ya existe un usuario con ese correo");
    }
}
