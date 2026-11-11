package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.category.CategoryRepository;

import com.alejandro.expense_tracker.exceptions.CategoryNotFoundException;
import com.alejandro.expense_tracker.exceptions.InvalidMovementTypeException;
import com.alejandro.expense_tracker.movement.dto.MovementRequest;
import com.alejandro.expense_tracker.movement.dto.MovementResponse;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

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
    public Page<MovementResponse> findAll(Long userId, Pageable pageable) {
        return movementRepository
                .findAllByUser_IdOrderByMovementDateDescIdDesc(userId, pageable)
                .map(this::toResponse);
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
