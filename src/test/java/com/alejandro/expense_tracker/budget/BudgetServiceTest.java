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
import com.alejandro.expense_tracker.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceTest {
    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void rejectsIncomeCategory() {
        Long userId = 5L;

        Category category = mock(Category.class);

        // La categoría pertenece al usuario, pero es de ingreso.
        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(category));

        when(category.getType()).thenReturn(CategoryType.INCOME);

        BudgetRequest request = new BudgetRequest(
                12L,
                YearMonth.of(2026, 11),
                new BigDecimal("250.00")
        );

        // No permitimos presupuestos para categorías de ingreso.
        assertThrows(
                InvalidBudgetCategoryException.class,
                () -> budgetService.create(userId, request)
        );

        // El servicio debe detenerse antes de consultar o guardar presupuestos.
        verifyNoInteractions(budgetRepository, movementRepository);
    }

    @Test
    void rejectsDuplicateBudgetForSameCategoryAndMonth() {
        Long userId = 5L;

        Category category = mock(Category.class);

        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(category));

        when(category.getType()).thenReturn(CategoryType.EXPENSE);
        when(category.getId()).thenReturn(12L);

        // Simulamos que ese usuario ya tiene presupuesto para noviembre.
        when(budgetRepository.existsByUser_IdAndCategory_IdAndMonth(
                userId,
                12L,
                LocalDate.of(2026, 11, 1)
        )).thenReturn(true);

        BudgetRequest request = new BudgetRequest(
                12L,
                YearMonth.of(2026, 11),
                new BigDecimal("250.00")
        );

        assertThrows(
                BudgetAlreadyExistsException.class,
                () -> budgetService.create(userId, request)
        );

        // Un duplicado no debe guardarse ni calcular sus gastos.
        verify(budgetRepository, never()).save(any(Budget.class));
        verifyNoInteractions(movementRepository);
    }

    @ParameterizedTest(
            name = "Gastado: {0}; disponible esperado: {1}"
    )
    @CsvSource({
            "0.00, 250.00",
            "135.50, 114.50",
            "280.00, -30.00"
    })
    void createsBudgetAndCalculatesRemainingAmount(
            String spent,
            String expectedRemaining
    ) {
        Long userId = 5L;

        User user = mock(User.class);
        Category category = mock(Category.class);

        // La categoría es de gasto y pertenece al usuario solicitado.
        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(category));

        when(category.getType()).thenReturn(CategoryType.EXPENSE);
        when(category.getId()).thenReturn(12L);
        when(category.getName()).thenReturn("Alimentación");
        when(category.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(userId);

        // No existe otro presupuesto para esa categoría y mes.
        when(budgetRepository.existsByUser_IdAndCategory_IdAndMonth(
                userId,
                12L,
                LocalDate.of(2026, 11, 1)
        )).thenReturn(false);

        // El repositorio simulado devuelve el presupuesto recibido.
        when(budgetRepository.save(any(Budget.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Simulamos el gasto acumulado dentro de noviembre.
        when(movementRepository.sumAmount(
                userId,
                12L,
                MovementType.EXPENSE,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 1)
        )).thenReturn(new BigDecimal(spent));

        BudgetRequest request = new BudgetRequest(
                12L,
                YearMonth.of(2026, 11),
                new BigDecimal("250.00")
        );

        BudgetResponse response = budgetService.create(userId, request);

        // Capturamos el presupuesto que se ha intentado guardar.
        ArgumentCaptor<Budget> captor =
                ArgumentCaptor.forClass(Budget.class);

        verify(budgetRepository).save(captor.capture());

        Budget savedBudget = captor.getValue();

        // Comprobamos el propietario, la categoría y el mes almacenado.
        assertSame(user, savedBudget.getUser());
        assertSame(category, savedBudget.getCategory());
        assertEquals(
                LocalDate.of(2026, 11, 1),
                savedBudget.getMonth()
        );
        assertEquals(
                new BigDecimal("250.00"),
                savedBudget.getLimitAmount()
        );

        // Comprobamos los importes calculados para la respuesta.
        assertEquals(
                new BigDecimal("250.00"),
                response.limitAmount()
        );
        assertEquals(
                new BigDecimal(spent),
                response.spentAmount()
        );
        assertEquals(
                new BigDecimal(expectedRemaining),
                response.remainingAmount()
        );
        assertEquals(YearMonth.of(2026, 11), response.month());
    }

    @Test
    void rejectsCategoryNotAvailableToUser() {
        Long userId = 5L;
        Long categoryId = 12L;

        BudgetRequest request = new BudgetRequest(
                categoryId,
                YearMonth.of(2026, 11),
                new BigDecimal("250.00")
        );

        // La búsqueda no encuentra la categoría para este propietario.
        when(categoryRepository.findByIdAndUser_Id(categoryId, userId))
                .thenReturn(Optional.empty());

        // El servicio debe rechazar la creación del presupuesto.
        assertThrows(
                CategoryNotFoundException.class,
                () -> budgetService.create(userId, request)
        );

        // Debe detenerse antes de consultar presupuestos o calcular gastos.
        verifyNoInteractions(budgetRepository, movementRepository);
    }

    @Test
    void updatesLimitAndRecalculatesRemainingAmount() {
        Long userId = 5L;
        Long budgetId = 20L;

        User user = mock(User.class);
        Category category = mock(Category.class);

        // El presupuesto empieza con un límite de 250 euros.
        Budget budget = new Budget(
                user,
                category,
                LocalDate.of(2026, 11, 1),
                new BigDecimal("250.00")
        );

        when(budgetRepository.findByIdAndUser_Id(budgetId, userId))
                .thenReturn(Optional.of(budget));

        // Estos datos se utilizan para calcular y construir la respuesta.
        when(user.getId()).thenReturn(userId);
        when(category.getId()).thenReturn(12L);
        when(category.getName()).thenReturn("Alimentación");

        when(movementRepository.sumAmount(
                userId,
                12L,
                MovementType.EXPENSE,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 12, 1)
        )).thenReturn(new BigDecimal("135.50"));

        BudgetLimitRequest request = new BudgetLimitRequest(
                new BigDecimal("300.00")
        );

        BudgetResponse response =
                budgetService.updateLimit(userId, budgetId, request);

        // El límite debe cambiar en la entidad.
        assertEquals(
                new BigDecimal("300.00"),
                budget.getLimitAmount()
        );

        // El propietario, la categoría y el mes deben conservarse.
        assertSame(user, budget.getUser());
        assertSame(category, budget.getCategory());
        assertEquals(
                LocalDate.of(2026, 11, 1),
                budget.getMonth()
        );

        // El gasto sigue siendo el mismo, pero cambia el disponible.
        assertEquals(new BigDecimal("300.00"), response.limitAmount());
        assertEquals(new BigDecimal("135.50"), response.spentAmount());
        assertEquals(new BigDecimal("164.50"), response.remainingAmount());
    }

    @Test
    void rejectsLimitUpdateWhenBudgetIsNotAvailableToUser() {
        Long userId = 5L;
        Long budgetId = 20L;

        BudgetLimitRequest request = new BudgetLimitRequest(
                new BigDecimal("300.00")
        );

        // No encontramos el presupuesto para este propietario.
        when(budgetRepository.findByIdAndUser_Id(budgetId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.updateLimit(userId, budgetId, request)
        );

        // No debe continuar con consultas a categorías o movimientos.
        verifyNoInteractions(categoryRepository, movementRepository);
    }

    @Test
    void deletesBudgetOwnedByUser() {
        Long userId = 5L;
        Long budgetId = 20L;

        Budget budget = mock(Budget.class);

        // La búsqueda encuentra el presupuesto del usuario.
        when(budgetRepository.findByIdAndUser_Id(budgetId, userId))
                .thenReturn(Optional.of(budget));

        budgetService.delete(userId, budgetId);

        // Comprobamos que solicita eliminar el presupuesto encontrado.
        verify(budgetRepository).delete(budget);

        // Eliminar el presupuesto no debe realizar operaciones en estos repositorios.
        verifyNoInteractions(categoryRepository, movementRepository);
    }

    @Test
    void rejectsDeletionWhenBudgetIsNotAvailableToUser() {
        Long userId = 5L;
        Long budgetId = 20L;

        when(budgetRepository.findByIdAndUser_Id(budgetId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                BudgetNotFoundException.class,
                () -> budgetService.delete(userId, budgetId)
        );

        // No debe intentar eliminar ningún presupuesto.
        verify(budgetRepository, never()).delete(any(Budget.class));
    }

}
