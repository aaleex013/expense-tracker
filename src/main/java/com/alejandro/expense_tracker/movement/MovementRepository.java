package com.alejandro.expense_tracker.movement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {
    Optional<Movement> findByIdAndUser_Id(Long id, Long userId);

}
