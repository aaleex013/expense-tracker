package com.alejandro.expense_tracker.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findAllByUser_IdAndMonthOrderByIdAsc(
            Long userId,
            LocalDate month
    );

    Optional<Budget> findByIdAndUser_Id(
            Long id,
            Long userId
    );

    boolean existsByUser_IdAndCategory_IdAndMonth(
            Long userId,
            Long categoryId,
            LocalDate month
    );
}
