package com.alejandro.expense_tracker.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100)
        String username,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El formato del correo no es válido")
        @Size(max = 254)
        String email,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 12, max = 128,
                message = "La contraseña debe tener mas de 12 caracteres")
        String password
) {
}
