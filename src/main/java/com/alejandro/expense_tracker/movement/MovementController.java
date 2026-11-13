package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.movement.dto.MovementPageResponse;
import com.alejandro.expense_tracker.movement.dto.MovementRequest;
import com.alejandro.expense_tracker.movement.dto.MovementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("api/movements")
@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Movimientos",
        description = "Creación, consulta, filtrado, edición y eliminación de movimientos"
)
public class MovementController {
    private final MovementService movementService;

    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar un movimiento",
            description = "Registra un ingreso o gasto. La categoría debe pertenecer al usuario autenticado y tener el mismo tipo que el movimiento."
    )
    public MovementResponse createMovement(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MovementRequest request) {
        Long userId = getUserId(jwt);

        return movementService.createMovement(userId, request);
    }

    @GetMapping
    @Operation(
            summary = "Listar y filtrar mis movimientos",
            description = "Devuelve una página de movimientos del usuario autenticado. Permite filtrar por tipo, categoría y fechas, incluyendo ambos días límite. Ordena por fecha e identificador descendentes."
    )
    public MovementPageResponse findAll(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false)
            MovementType type,
            @RequestParam(required = false)
            Long categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La página debe ser mayor o igual a 0 y el tamaño entre 1 y 100"
            );
        }
        if (categoryId != null && categoryId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de la categoría debe ser positivo"
            );
        }

        Long userId = getUserId(jwt);

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "movementDate",
                        "id"
                )
        );

        Page<MovementResponse> result = movementService.findAll(
                userId,
                type,
                categoryId,
                fromDate,
                toDate,
                pageable
        );

        return new MovementPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar un movimiento",
            description = "Devuelve un movimiento por su identificador, siempre que pertenezca al usuario autenticado."
    )
    public MovementResponse findById(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long movementId) {
        Long userId = getUserId(jwt);

        return movementService.findById(userId, movementId);
    }
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un movimiento",
            description = "Actualiza la categoría, el tipo, el importe, la fecha y la descripción de un movimiento propio. Deben enviarse todos los campos obligatorios."
    )
    public MovementResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long movementId,
            @Valid @RequestBody MovementRequest request
    ) {
        Long userId = getUserId(jwt);

        return movementService.update(userId, movementId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Eliminar un movimiento",
            description = "Elimina definitivamente un movimiento propio. Los movimientos asociados a un vencimiento pagado están protegidos por su relación con ese pago."
    )
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long movementId) {
        Long userId = getUserId(jwt);

        movementService.delete(userId, movementId);
    }

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
