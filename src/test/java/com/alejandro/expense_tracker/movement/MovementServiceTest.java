package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.category.CategoryRepository;
import com.alejandro.expense_tracker.category.CategoryType;
import com.alejandro.expense_tracker.exceptions.CategoryNotFoundException;
import com.alejandro.expense_tracker.exceptions.InvalidDateRangeException;
import com.alejandro.expense_tracker.exceptions.InvalidMovementTypeException;
import com.alejandro.expense_tracker.exceptions.MovementNotFoundException;
import com.alejandro.expense_tracker.movement.dto.MovementRequest;
import com.alejandro.expense_tracker.movement.dto.MovementResponse;
import com.alejandro.expense_tracker.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovementServiceTest {
    @Mock
    private MovementRepository movementRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MovementService movementService;

    @Test
    void rejectsCategoryNotAvailableToUser() {
        // Preparar: el repositorio no encuentra la categoría para este usuario.
        Long userId = 5L;

        MovementRequest request = new MovementRequest(
                12L,
                MovementType.EXPENSE,
                new BigDecimal("42.50"),
                LocalDate.of(2026, 11, 12),
                "Compra semanal"
        );

        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.empty());

        // Ejecutar y comprobar que se rechaza la operación.
        assertThrows(
                CategoryNotFoundException.class,
                () -> movementService.createMovement(userId, request)
        );

        // No debe intentar guardar ningún movimiento.
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void rejectsExpenseWithIncomeCategory() {
        // Preparar: la categoría existe, pero es de ingreso.
        Long userId = 5L;

        Category category = mock(Category.class);

        when(category.getType()).thenReturn(CategoryType.INCOME);

        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(category));

        MovementRequest request = new MovementRequest(
                12L,
                MovementType.EXPENSE,
                new BigDecimal("42.50"),
                LocalDate.of(2026, 11, 12),
                "Compra semanal"
        );

        // Un movimiento de gasto no puede utilizar una categoría de ingreso.
        assertThrows(
                InvalidMovementTypeException.class,
                () -> movementService.createMovement(userId, request)
        );

        verify(movementRepository, never()).save(any(Movement.class));
    }
    @Test
    void createsMovementWithCategoryOwnedByUser() {
        Long userId = 5L;

        User user = mock(User.class);
        Category category = mock(Category.class);

        // La búsqueda encuentra una categoría del usuario.
        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(category));

        when(category.getType()).thenReturn(CategoryType.EXPENSE);
        when(category.getUser()).thenReturn(user);
        when(category.getId()).thenReturn(12L);
        when(category.getName()).thenReturn("Alimentación");

        MovementRequest request = new MovementRequest(
                12L,
                MovementType.EXPENSE,
                new BigDecimal("42.50"),
                LocalDate.of(2026, 11, 12),
                "Compra semanal"
        );

        // El repositorio simulado devuelve el mismo movimiento que recibe.
        when(movementRepository.save(any(Movement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MovementResponse response =
                movementService.createMovement(userId, request);

        // Capturamos el movimiento que el servicio ha intentado guardar.
        ArgumentCaptor<Movement> captor =
                ArgumentCaptor.forClass(Movement.class);

        verify(movementRepository).save(captor.capture());

        Movement savedMovement = captor.getValue();

        // Comprobamos propietario, categoría y datos del movimiento.
        assertSame(user, savedMovement.getUser());
        assertSame(category, savedMovement.getCategory());
        assertEquals(MovementType.EXPENSE, savedMovement.getType());
        assertEquals(new BigDecimal("42.50"), savedMovement.getAmount());
        assertEquals(
                LocalDate.of(2026, 11, 12),
                savedMovement.getMovementDate()
        );

        // Comprobamos parte de la respuesta que recibirá el cliente.
        assertEquals(Long.valueOf(12L), response.categoryId());
        assertEquals("Alimentación", response.categoryName());
        assertEquals(new BigDecimal("42.50"), response.amount());
    }
    @Test
    void rejectsStartDateAfterEndDate() {
        Long userId = 5L;

        LocalDate fromDate = LocalDate.of(2026, 11, 30);
        LocalDate toDate = LocalDate.of(2026, 11, 1);

        assertThrows(
                InvalidDateRangeException.class,
                () -> movementService.findAll(
                        userId,
                        null,
                        null,
                        fromDate,
                        toDate,
                        PageRequest.of(0, 20)
                )
        );

        // El intervalo debe rechazarse antes de consultar los movimientos.
        verifyNoInteractions(movementRepository);
    }
    @Test
    void updatesMovementOwnedByUser() {
        Long userId = 5L;
        Long movementId = 15L;

        User user = mock(User.class);
        Category originalCategory = mock(Category.class);
        Category newCategory = mock(Category.class);

        // Movimiento que ya existe antes de editarlo.
        Movement movement = new Movement(
                user,
                originalCategory,
                MovementType.EXPENSE,
                new BigDecimal("20.00"),
                LocalDate.of(2026, 11, 1),
                "Descripción original"
        );

        // La búsqueda encuentra el movimiento para este usuario.
        when(movementRepository.findByIdAndUser_Id(movementId, userId))
                .thenReturn(Optional.of(movement));

        // La nueva categoría también está disponible para este usuario.
        when(categoryRepository.findByIdAndUser_Id(12L, userId))
                .thenReturn(Optional.of(newCategory));

        when(newCategory.getType()).thenReturn(CategoryType.EXPENSE);
        when(newCategory.getId()).thenReturn(12L);
        when(newCategory.getName()).thenReturn("Ocio");

        MovementRequest request = new MovementRequest(
                12L,
                MovementType.EXPENSE,
                new BigDecimal("35.50"),
                LocalDate.of(2026, 11, 12),
                "  Cena corregida  "
        );

        // Ejecutamos la actualización.
        MovementResponse response =
                movementService.update(userId, movementId, request);

        // Comprobamos los nuevos valores de la entidad.
        assertSame(newCategory, movement.getCategory());
        assertEquals(MovementType.EXPENSE, movement.getType());
        assertEquals(new BigDecimal("35.50"), movement.getAmount());
        assertEquals(
                LocalDate.of(2026, 11, 12),
                movement.getMovementDate()
        );
        assertEquals("Cena corregida", movement.getDescripcion());

        // El propietario debe conservarse.
        assertSame(user, movement.getUser());

        // La respuesta también debe reflejar los cambios.
        assertEquals(new BigDecimal("35.50"), response.amount());
        assertEquals("Ocio", response.categoryName());
    }
    @Test
    void rejectsUpdateWhenMovementIsNotAvailableToUser() {
        Long userId = 5L;
        Long movementId = 15L;

        MovementRequest request = new MovementRequest(
                12L,
                MovementType.EXPENSE,
                new BigDecimal("35.50"),
                LocalDate.of(2026, 11, 12),
                "Intento de modificación"
        );

        // Simulamos que el movimiento no se encuentra para este propietario.
        when(movementRepository.findByIdAndUser_Id(movementId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovementNotFoundException.class,
                () -> movementService.update(userId, movementId, request)
        );

        // Debe rechazar la operación antes de buscar la nueva categoría.
        verifyNoInteractions(categoryRepository);

        // Tampoco debe intentar guardar un movimiento.
        verify(movementRepository, never()).save(any(Movement.class));
    }
    @Test
    void deletesMovementOwnedByUser() {
        Long userId = 5L;
        Long movementId = 15L;

        Movement movement = mock(Movement.class);

        // El repositorio encuentra el movimiento para este usuario.
        when(movementRepository.findByIdAndUser_Id(movementId, userId))
                .thenReturn(Optional.of(movement));

        movementService.delete(userId, movementId);

        // Debe eliminar exactamente el movimiento encontrado.
        verify(movementRepository).delete(movement);
    }
    @Test
    void rejectsDeletionWhenMovementIsNotAvailableToUser() {
        Long userId = 5L;
        Long movementId = 15L;

        // El movimiento no está disponible para este propietario.
        when(movementRepository.findByIdAndUser_Id(movementId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                MovementNotFoundException.class,
                () -> movementService.delete(userId, movementId)
        );

        // No debe intentar eliminar ningún movimiento.
        verify(movementRepository, never()).delete(any(Movement.class));
    }
}
