package com.alejandro.expense_tracker.recurring.occurrence;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.exceptions.ExpenseOccurrenceAlreadyPaidException;
import com.alejandro.expense_tracker.movement.Movement;
import com.alejandro.expense_tracker.recurring.RecurringExpense;
import com.alejandro.expense_tracker.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "expense_occurrences",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_expense_occurrences_recurring_date",
                columnNames = {
                        "recurring_expense_id",
                        "expiration_date"
                }
        )
)
public class ExpenseOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración que originó este vencimiento.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurring_expense_id", nullable = false)
    private RecurringExpense recurringExpense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Conservamos la categoría asignada al generar el vencimiento.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ExpenseOccurrenceStatus status =
            ExpenseOccurrenceStatus.PENDING;

    // Se asignará cuando confirmemos el pago
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movement_id", unique = true)
    private Movement movement;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ExpenseOccurrence() {
    }

    public ExpenseOccurrence(
            RecurringExpense recurringExpense,
            LocalDate expirationDate
    ) {
        this.recurringExpense = recurringExpense;
        this.expirationDate = expirationDate;

        // Copiamos los datos actuales para conservarlos en el vencimiento.
        this.user = recurringExpense.getUser();
        this.category = recurringExpense.getCategory();
        this.name = recurringExpense.getName();
        this.amount = recurringExpense.getAmount();
    }

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    // Vincula el movimiento creado y marca el vencimiento como pagado.
    public void confirmPayment(Movement movement) {
        if (status == ExpenseOccurrenceStatus.PAID) {
            throw new ExpenseOccurrenceAlreadyPaidException();
        }

        this.movement = movement;
        this.status = ExpenseOccurrenceStatus.PAID;
        this.paidAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public RecurringExpense getRecurringExpense() {
        return recurringExpense;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public ExpenseOccurrenceStatus getStatus() {
        return status;
    }

    public Movement getMovement() {
        return movement;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}