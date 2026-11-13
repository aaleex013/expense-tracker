package com.alejandro.expense_tracker.recurring.occurrence;

import com.alejandro.expense_tracker.recurring.occurrence.dto.ConfirmExpensePaymentRequest;
import com.alejandro.expense_tracker.recurring.occurrence.dto.ExpenseOccurrenceResponse;
import com.alejandro.expense_tracker.recurring.occurrence.dto.GenerateOccurrencesRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/expense-occurrences")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Vencimientos",
        description = "Generación y consulta de vencimientos y confirmación de pagos asociados"
)
public class ExpenseOccurrenceController {

    private final ExpenseOccurrenceService occurrenceService;

    // Spring proporciona el servicio al crear el controlador.
    public ExpenseOccurrenceController(
            ExpenseOccurrenceService occurrenceService
    ) {
        this.occurrenceService = occurrenceService;
    }

    // Genera los vencimientos que todavía no existan para el mes solicitado.
    @PostMapping("/generate")
    @Operation(
            summary = "Generar vencimientos de un mes",
            description = "Genera los vencimientos pendientes que correspondan a los gastos recurrentes activos del usuario. Respeta la frecuencia y las fechas, ajusta los días inexistentes al último día del mes y devuelve únicamente los nuevos vencimientos."
    )
    public List<ExpenseOccurrenceResponse> generateForMonth(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GenerateOccurrencesRequest request
    ) {
        Long userId = getUserId(jwt);

        return occurrenceService.generateForMonth(userId, request);
    }

    // Consulta los vencimientos del mes, tanto pendientes como pagados.
    @GetMapping
    @Operation(
            summary = "Consultar mis vencimientos del mes",
            description = "Devuelve los vencimientos pendientes y pagados del usuario para el mes indicado, ordenados por fecha de vencimiento e identificador ascendentes."
    )
    public List<ExpenseOccurrenceResponse> findAll(
            @AuthenticationPrincipal Jwt jwt,

            // El mes se recibe con formato año-mes, por ejemplo 2026-12.
            @RequestParam("month")
            @DateTimeFormat(pattern = "uuuu-MM")
            YearMonth month
    ) {
        Long userId = getUserId(jwt);

        return occurrenceService.findAll(userId, month);
    }

    // Confirma el pago y crea el movimiento de gasto asociado.
    @PostMapping("/{id}/pay")
    @Operation(
            summary = "Confirmar el pago de un vencimiento",
            description = "Marca un vencimiento propio como pagado y crea el movimiento de gasto asociado con la fecha de pago indicada. No permite confirmar dos veces el mismo vencimiento."
    )
    public ExpenseOccurrenceResponse confirmPayment(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") Long occurrenceId,
            @Valid @RequestBody ConfirmExpensePaymentRequest request
    ) {
        Long userId = getUserId(jwt);

        return occurrenceService.confirmPayment(
                userId,
                occurrenceId,
                request
        );
    }

    // Obtenemos el propietario desde el JWT validado por Spring Security.
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