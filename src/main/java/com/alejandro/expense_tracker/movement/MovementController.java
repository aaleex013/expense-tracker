package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.movement.dto.MovementPageResponse;
import com.alejandro.expense_tracker.movement.dto.MovementRequest;
import com.alejandro.expense_tracker.movement.dto.MovementResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
public class MovementController {
    private final MovementService movementService;

    public MovementController(MovementService movementService) {
        this.movementService = movementService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovementResponse createMovement(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MovementRequest request) {
        Long userId = getUserId(jwt);

        return movementService.createMovement(userId, request);
    }

    @GetMapping
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
    public MovementResponse findById(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long movementId) {
        Long userId = getUserId(jwt);

        return movementService.findById(userId, movementId);
    }
    @PutMapping("/{id}")
    public MovementResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long movementId,
            @Valid @RequestBody MovementRequest request
    ) {
        Long userId = getUserId(jwt);

        return movementService.update(userId, movementId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
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
