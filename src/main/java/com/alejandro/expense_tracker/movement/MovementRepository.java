package com.alejandro.expense_tracker.movement;


import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    Page<Movement> findAllByUser_IdOrderByMovementDateDescIdDesc(Long userId, Pageable pageable);
    Optional<Movement> findByIdAndUser_Id(Long id, Long userId);

}
