package com.alejandro.expense_tracker.recurring;

import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseActiveRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseRequest;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Gastos recurrentes",
        description = "Gestión de gastos recurrentes mensuales y anuales, con activación y desactivación"
)
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
    @Operation(
            summary = "Crear un gasto recurrente",
            description = "Configura un gasto mensual o anual utilizando una categoría de gasto propia. Los anuales deben indicar el mes del año. No genera vencimientos ni movimientos automáticamente."
    )
    public RecurringExpenseResponse create(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody RecurringExpenseRequest request
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.create(userId, request);
    }


    // Lista todos los gastos recurrentes o únicamente los activos.
    @GetMapping
    @Operation(
            summary = "Listar mis gastos recurrentes",
            description = "Devuelve los gastos recurrentes del usuario autenticado. Con activeOnly=true devuelve únicamente los activos."
    )
    public List<RecurringExpenseResponse> findAll(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        Long userId = getUserId(jwt);

        return recurringExpenseService.findAll(userId, activeOnly);
    }

    // Actualiza los datos de un gasto recurrente del usuario.
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un gasto recurrente",
            description = "Actualiza la configuración de un gasto recurrente propio. Los vencimientos ya generados conservan su nombre, categoría e importe."
    )
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
    @Operation(
            summary = "Eliminar un gasto recurrente",
            description = "Elimina una configuración propia que no tenga vencimientos asociados. Si ya tiene vencimientos, puede desactivarse para detener nuevas generaciones y conservar el historial."
    )
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long recurringExpenseId) {
        Long userId = getUserId(jwt);

        recurringExpenseService.delete(userId, recurringExpenseId);
    }

    // Cambia únicamente el estado activo del gasto recurrente.
    @PatchMapping("/{id}/active")
    @Operation(
            summary = "Activar o desactivar un gasto recurrente",
            description = "Cambia el estado activo de un gasto recurrente propio. Desactivarlo evita generar nuevos vencimientos, pero conserva los que ya existen."
    )
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
