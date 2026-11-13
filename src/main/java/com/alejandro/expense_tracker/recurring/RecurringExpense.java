package com.alejandro.expense_tracker.recurring;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.recurring.dto.RecurringExpenseFrequency;
import com.alejandro.expense_tracker.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "recurring_expenses")
public class RecurringExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario propietario de la regla.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Categoría que utilizaremos al registrar los pagos.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // Día mensual solicitado: un número entre 1 y 31.
    @Column(name = "day_of_month", nullable = false)
    private Short dayOfMonth;
    // Indica si el gasto se repite cada mes o cada año.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecurringExpenseFrequency frequency;

    // Solo se utiliza en gastos anuales: enero = 1, diciembre = 12.
    @Column(name = "month_of_year")
    private Short monthOfYear;

    // Los vencimientos deben estar dentro de este intervalo.
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Puede ser null cuando la regla no tiene fecha final.
    @Column(name = "end_date")
    private LocalDate endDate;

    // Las reglas nuevas empiezan activas.
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RecurringExpense() {
    }

    public RecurringExpense(User user, Category category, String name, BigDecimal amount, RecurringExpenseFrequency frequency, Short dayOfMonth, Short monthOfYear, LocalDate startDate, LocalDate endDate
    ) {
        this.user = user;
        this.category = category;
        this.name = name;
        this.amount = amount;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @PrePersist
    private void onCreated() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
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

    public Short getDayOfMonth() {
        return dayOfMonth;
    }
    public RecurringExpenseFrequency getFrequency() {
        return frequency;
    }

    public Short getMonthOfYear() {
        return monthOfYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Actualiza los datos editables del gasto recurrente
    // El propietario, el ID y la fecha de creación se conservan
    public void updateDetails(
            Category category,
            String name,
            BigDecimal amount,
            RecurringExpenseFrequency frequency,
            Short dayOfMonth,
            Short monthOfYear,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.category = category;
        this.name = name;
        this.amount = amount;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    // Permite activar o desactivar el gasto recurrente sin borrar sus datos.
    public void updateActive(boolean active) {
        this.active = active;
    }
}
