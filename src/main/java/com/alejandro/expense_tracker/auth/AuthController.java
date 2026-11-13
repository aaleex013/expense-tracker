package com.alejandro.expense_tracker.auth;


import com.alejandro.expense_tracker.auth.dto.LoginRequest;
import com.alejandro.expense_tracker.auth.dto.LoginResponse;
import com.alejandro.expense_tracker.auth.dto.RegisterRequest;
import com.alejandro.expense_tracker.auth.dto.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticación",
        description = "Registro de usuarios e inicio de sesión mediante JWT"
)
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar un usuario",
            description = "Crea una cuenta y sus categorías iniciales. El correo debe ser único."
    )
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return authService.register(request);
    }
    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Comprueba el correo y la contraseña y devuelve un JWT para acceder a los endpoints protegidos."
    )
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authService.login(request);
    }
}
