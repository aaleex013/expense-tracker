package com.alejandro.expense_tracker.report;

import com.alejandro.expense_tracker.report.dto.MonthlyReportResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    // Devuelve el informe mensual del usuario autenticado.
    @GetMapping("/monthly")
    public MonthlyReportResponse getMonthlyReport(
            @AuthenticationPrincipal Jwt jwt,

            // Recibimos el mes con formato año-mes, por ejemplo 2026-11.
            @RequestParam("month")
            //uuu es año
            @DateTimeFormat(pattern = "uuuu-MM")
            YearMonth month
    ) {
        // El propietario se obtiene del token.
        Long userId = getUserId(jwt);

        return reportService.getMonthlyReport(userId, month);
    }

    // Convertimos el identificador del JWT de texto a Long.
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