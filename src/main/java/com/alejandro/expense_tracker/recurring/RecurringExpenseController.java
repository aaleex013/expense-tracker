package com.alejandro.expense_tracker.recurring;

import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseActiveRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("api/recurring-expenses")
public class RecurringExpenseController {
    @Autowired
    private RecurringExpenseService recurringExpenseService;

    public RecurringExpenseController(
            RecurringExpenseService recurringExpenseService
    ) {
        this.recurringExpenseService = recurringExpenseService;
    }

    // Crea la configuración de un gasto recurrente mensual.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringExpenseResponse create(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody RecurringExpenseRequest request
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.create(userId, request);
    }


    // Lista todos los gastos recurrentes o únicamente los activos.
    @GetMapping
    public List<RecurringExpenseResponse> findAll(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.findAll(userId, activeOnly);
    }

    // Actualiza los datos de un gasto recurrente del usuario.
    @PutMapping("/{id}")
    public RecurringExpenseResponse update(
            @AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long recurringExpenseId,
            @Valid @RequestBody RecurringExpenseRequest request
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.update(
                userId,
                recurringExpenseId,
                request
        );
    }

    // Elimina el gasto recurrente y responde sin cuerpo.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long recurringExpenseId) {
        Long userId = getUserId(jwt);

        recurringExpenseService.delete(userId, recurringExpenseId);
    }

    // Cambia únicamente el estado activo del gasto recurrente.
    @PatchMapping("/{id}/active")
    public RecurringExpenseResponse updateActive(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long recurringExpenseId,
            @Valid @RequestBody RecurringExpenseActiveRequest request
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.updateActive(
                userId,
                recurringExpenseId,
                request
        );
    }

    // Obtiene el propietario del token validado por Spring Security.
    private Long getUserId(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene el identificador del usuario"
            );
        }

        try {
            return Long.valueOf(subject);
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El identificador del token no es válido"
            );
        }
    }
}
