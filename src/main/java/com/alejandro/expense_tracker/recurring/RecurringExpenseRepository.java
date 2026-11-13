package com.alejandro.expense_tracker.recurring;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {
    // Devuelve todos los gastos recurrentes del usuario, ordenadas por nombre e ID.
    List<RecurringExpense> findAllByUser_IdOrderByNameAscIdAsc(
            Long userId
    );

    // Busca un gasto recurrente por su ID, comprobando que pertenezca al usuario
    Optional<RecurringExpense> findByIdAndUser_Id(
            Long id,
            Long userId
    );

    // / Devuelve los gastos recurrentes activos del usuario, ordenados por nombre e ID
    List<RecurringExpense> findAllByUser_IdAndActiveTrueOrderByNameAscIdAsc(
            Long userId
    );
    // Bloqueamos las configuraciones mientras generamos sus vencimientos.
    // Las procesamos por ID para mantener un orden consistente.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RecurringExpense> findAllByUser_IdAndActiveTrueOrderByIdAsc(
            Long userId
    );
}
