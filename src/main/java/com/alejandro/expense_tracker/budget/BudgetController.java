package com.alejandro.expense_tracker.budget;

import com.alejandro.expense_tracker.budget.dto.BudgetRequest;
import com.alejandro.expense_tracker.budget.dto.BudgetResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    // Spring proporciona el servicio que utilizará este controlador.
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // Crea un presupuesto y devuelve 201 si la operación termina correctamente.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody BudgetRequest request
    ) {
        // El propietario se obtiene del token, no del JSON recibido.
        Long userId = getUserId(jwt);

        return budgetService.create(userId, request);
    }

    // Consulta los presupuestos del usuario para el mes indicado.
    @GetMapping
    public List<BudgetResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,

            // Ejemplo: ?month=2026-11 representa noviembre de 2026.
            @RequestParam("month")
            @DateTimeFormat(pattern = "uuuu-MM")
            YearMonth month
    ) {
        Long userId = getUserId(jwt);

        return budgetService.findAll(userId, month);
    }

    // Extrae el identificador del usuario del JWT ya validado por Spring Security.
    private Long getUserId(Jwt jwt) {
        String subject = jwt.getSubject();

        // Rechazamos el token si no contiene el identificador.
        if (subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene el identificador del usuario"
            );
        }

        try {
            // El identificador del token es texto y el servicio necesita un Long.
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El identificador del token no es válido"
            );
        }
    }
}
