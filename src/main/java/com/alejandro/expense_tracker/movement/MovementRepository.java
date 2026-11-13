package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.report.dto.CategoryExpenseResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    // Suma los movimientos del usuario según su tipo y periodo
    // Y devuelve cero cuando no existen movimientos coincidentes
    @Query("""
            SELECT COALESCE(SUM(m.amount), 0)
            FROM Movement m
            WHERE m.user.id = :userId
              AND m.type = :type
              AND m.movementDate >= :fromDate
              AND m.movementDate < :toDateExclusive
            """)
    BigDecimal sumAmountForPeriod(
            @Param("userId") Long userId,
            @Param("type") MovementType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDateExclusive") LocalDate toDateExclusive
    );
    // Agrupa los movimientos por categoría y suma sus importes.
// En el informe la utilizaremos pasando el tipo EXPENSE.
    @Query("""
        SELECT new com.alejandro.expense_tracker.report.dto.CategoryExpenseResponse(
            m.category.id,
            m.category.name,
            SUM(m.amount)
        )
        FROM Movement m
        WHERE m.user.id = :userId
          AND m.type = :type
          AND m.movementDate >= :fromDate
          AND m.movementDate < :toDateExclusive
        GROUP BY m.category.id, m.category.name
        ORDER BY SUM(m.amount) DESC, m.category.id ASC
        """)
    List<CategoryExpenseResponse> findExpenseTotalsByCategory(
            @Param("userId") Long userId,
            @Param("type") MovementType type,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDateExclusive") LocalDate toDateExclusive
    );

}
