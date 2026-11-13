package com.alejandro.expense_tracker.budget;

import com.alejandro.expense_tracker.budget.dto.BudgetLimitRequest;
import com.alejandro.expense_tracker.budget.dto.BudgetRequest;
import com.alejandro.expense_tracker.budget.dto.BudgetResponse;
import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.category.CategoryRepository;
import com.alejandro.expense_tracker.category.CategoryType;
import com.alejandro.expense_tracker.exceptions.BudgetAlreadyExistsException;
import com.alejandro.expense_tracker.exceptions.BudgetNotFoundException;
import com.alejandro.expense_tracker.exceptions.CategoryNotFoundException;
import com.alejandro.expense_tracker.exceptions.InvalidBudgetCategoryException;
import com.alejandro.expense_tracker.movement.MovementRepository;
import com.alejandro.expense_tracker.movement.MovementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final MovementRepository movementRepository;

    public BudgetService(
            BudgetRepository budgetRepository,
            CategoryRepository categoryRepository,
            MovementRepository movementRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.movementRepository = movementRepository;
    }

    @Transactional
    public BudgetResponse create(
            Long userId,
            BudgetRequest request
    ) {
        // La categoría debe existir y pertenecer al usuario.
        Category category = categoryRepository
                .findByIdAndUser_Id(request.categoryId(), userId)
                .orElseThrow(CategoryNotFoundException::new);

        // Los presupuestos solo se aplican a gastos.
        if (category.getType() != CategoryType.EXPENSE) {
            throw new InvalidBudgetCategoryException();
        }

        // Guardamos el mes como una fecha con día 1.
        LocalDate month = request.month().atDay(1);

        boolean alreadyExists = budgetRepository
                .existsByUser_IdAndCategory_IdAndMonth(
                        userId,
                        category.getId(),
                        month
                );

        if (alreadyExists) {
            throw new BudgetAlreadyExistsException();
        }

        Budget budget = new Budget(
                category.getUser(),
                category,
                month,
                request.limitAmount()
        );

        Budget savedBudget = budgetRepository.save(budget);

        return toResponse(savedBudget);
    }
    @Transactional(readOnly = true)
    public List<BudgetResponse> findAll(
            Long userId,
            YearMonth month
    ) {
        return budgetRepository
                .findAllByUser_IdAndMonthOrderByIdAsc(
                        userId,
                        month.atDay(1)
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BudgetResponse updateLimit(
            Long userId,
            Long budgetId,
            BudgetLimitRequest request
    ) {
        // Buscamos el presupuesto comprobando también su propietario.
        Budget budget = budgetRepository
                .findByIdAndUser_Id(budgetId, userId)
                .orElseThrow(BudgetNotFoundException::new);

        budget.updateLimitAmount(request.limitAmount());

        // Hibérnate guardará el cambio al confirmar la transacción.
        // La respuesta vuelve a calcular lo gastado y lo disponible.
        return toResponse(budget);
    }

    @Transactional
    public void delete(Long userId, Long budgetId) {
        // Un usuario solo puede eliminar sus propios presupuestos.
        Budget budget = budgetRepository
                .findByIdAndUser_Id(budgetId, userId)
                .orElseThrow(BudgetNotFoundException::new);

        budgetRepository.delete(budget);
    }

    private BudgetResponse toResponse(Budget budget) {
        LocalDate fromDate = budget.getMonth();
        LocalDate toDateExclusive = fromDate.plusMonths(1);

        // Sumamos los gastos del propietario, categoría y mes.
        BigDecimal spentAmount = movementRepository.sumAmount(
                budget.getUser().getId(),
                budget.getCategory().getId(),
                MovementType.EXPENSE,
                fromDate,
                toDateExclusive
        );

        BigDecimal remainingAmount = budget.getLimitAmount()
                .subtract(spentAmount);

        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                YearMonth.from(budget.getMonth()),
                budget.getLimitAmount(),
                spentAmount,
                remainingAmount,
                budget.getCreatedAt()
        );
    }
}
