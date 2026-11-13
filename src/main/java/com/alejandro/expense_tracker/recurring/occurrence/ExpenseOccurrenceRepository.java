package com.alejandro.expense_tracker.recurring.occurrence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseOccurrenceRepository extends JpaRepository<ExpenseOccurrence, Long> {
    // Comprueba si ya generamos este vencimiento.
    boolean existsByRecurringExpense_IdAndExpirationDate(
            Long recurringExpenseId,
            LocalDate expirationDate
    );

    // Bloqueamos el vencimiento mientras se confirma su pago.
    // Otra confirmación deberá esperar a que termine esta transacción.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ExpenseOccurrence> findByIdAndUser_Id(
            Long id,
            Long userId
    );

    // Consulta los vencimientos del usuario dentro del intervalo solicitado.
    @Query("""
            SELECT o
            FROM ExpenseOccurrence o
            WHERE o.user.id = :userId
              AND o.expirationDate >= :fromDate
              AND o.expirationDate < :toDateExclusive
            ORDER BY o.expirationDate ASC, o.id ASC
            """)
    List<ExpenseOccurrence> findForPeriod(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDateExclusive") LocalDate toDateExclusive
    );
}
