package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.category.CategoryRepository;

import com.alejandro.expense_tracker.exceptions.CategoryNotFoundException;
import com.alejandro.expense_tracker.exceptions.InvalidDateRangeException;
import com.alejandro.expense_tracker.exceptions.InvalidMovementTypeException;
import com.alejandro.expense_tracker.exceptions.MovementNotFoundException;
import com.alejandro.expense_tracker.movement.dto.MovementRequest;
import com.alejandro.expense_tracker.movement.dto.MovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Service
public class MovementService {
    private final MovementRepository movementRepository;
    private final CategoryRepository categoryRepository;

    public MovementService(MovementRepository movementRepository, CategoryRepository categoryRepository) {
        this.movementRepository = movementRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public MovementResponse createMovement(Long user_id, MovementRequest movementRequest) {
        Category category = categoryRepository.findByIdAndUser_Id(movementRequest.categoryId(), user_id)
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.getType().name()
                .equals(movementRequest.type().name())) {
            throw new InvalidMovementTypeException();
        }

        String descripcion = movementRequest.descripcion() == null
                ? null
                : movementRequest.descripcion().strip();

        Movement movement = new Movement(
                category.getUser(),
                category,
                movementRequest.type(),
                movementRequest.amount(),
                movementRequest.movementDate(),
                descripcion
        );
        Movement savedMovement = movementRepository.save(movement);
        return toResponse(savedMovement);


    }

    @Transactional(readOnly = true)
    public Page<MovementResponse> findAll(
            Long userId,
            MovementType type,
            Long categoryId,
            LocalDate fromDate,
            LocalDate toDate,
            Pageable pageable
    ) {
        // Comprobamos el intervalo solo cuando se han indicado ambas fechas.
        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new InvalidDateRangeException();
        }

        // Construimos las condiciones, incluyendo siempre al propietario.
        Specification<Movement> specification =
                MovementSpecifications.withFilters(
                        userId,
                        type,
                        categoryId,
                        fromDate,
                        toDate
                );

        // Ejecutamos la consulta con filtros y paginación.
        return movementRepository.findAll(specification, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public MovementResponse findById(Long userid, Long movementId) {
        Movement movement = movementRepository
                .findByIdAndUser_Id(movementId, userid)
                .orElseThrow(MovementNotFoundException::new);

        return toResponse(movement);
    }

    @Transactional
    public MovementResponse update(Long userId, Long movementId, MovementRequest request) {
        Movement movement = movementRepository
                .findByIdAndUser_Id(movementId, userId)
                .orElseThrow(MovementNotFoundException::new);

        Category category = categoryRepository
                .findByIdAndUser_Id(request.categoryId(), userId)
                .orElseThrow(CategoryNotFoundException::new);

        if (!category.getType().name().equals(request.type().name())) {
            throw new InvalidMovementTypeException();
        }

        String descripcion = request.descripcion() == null
                ? null
                : request.descripcion().strip();

        movement.updateDetails(
                category,
                request.type(),
                request.amount(),
                request.movementDate(),
                descripcion
        );

        return toResponse(movement);
    }

    @Transactional
    public void delete(Long userId, Long movementId) {
        Movement movement = movementRepository
                .findByIdAndUser_Id(movementId, userId)
                .orElseThrow(MovementNotFoundException::new);

        movementRepository.delete(movement);
    }

    private MovementResponse toResponse(Movement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getCategory().getId(),
                movement.getCategory().getName(),
                movement.getType(),
                movement.getAmount(),
                movement.getMovementDate(),
                movement.getDescripcion(),
                movement.getCreatedAt()
        );
    }
}
