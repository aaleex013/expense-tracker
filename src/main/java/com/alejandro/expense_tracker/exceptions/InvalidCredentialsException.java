package com.alejandro.expense_tracker.exceptions;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException() {
        super("correo o contraseña incorrectos");
    }
}
