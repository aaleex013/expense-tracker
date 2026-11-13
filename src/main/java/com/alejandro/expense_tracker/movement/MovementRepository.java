package com.alejandro.expense_tracker.movement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface MovementRepository extends JpaRepository<Movement, Long>, JpaSpecificationExecutor<Movement> {
    Optional<Movement> findByIdAndUser_Id(Long id, Long userId);
    @Query("""
        SELECT COALESCE(SUM(m.amount), 0)
        FROM Movement m
        WHERE m.user.id = :userId
          AND m.category.id = :categoryId
          AND m.type = :type
          AND m.movementDate >= :fromDate
          AND m.movementDate < :toDateExclusive
        """)
    BigDecimal sumAmount(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("type") MovementType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDateExclusive") LocalDate toDateExclusive
    );

}
