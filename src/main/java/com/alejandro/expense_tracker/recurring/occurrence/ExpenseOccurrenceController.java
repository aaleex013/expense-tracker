package com.alejandro.expense_tracker.recurring.occurrence;

import com.alejandro.expense_tracker.recurring.occurrence.dto.ConfirmExpensePaymentRequest;
import com.alejandro.expense_tracker.recurring.occurrence.dto.ExpenseOccurrenceResponse;
import com.alejandro.expense_tracker.recurring.occurrence.dto.GenerateOccurrencesRequest;
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
    public List<ExpenseOccurrenceResponse> generateForMonth(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody GenerateOccurrencesRequest request
    ) {
        Long userId = getUserId(jwt);

        return occurrenceService.generateForMonth(userId, request);
    }

    // Consulta los vencimientos del mes, tanto pendientes como pagados.
    @GetMapping
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