package com.alejandro.expense_tracker.budget;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "budgets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_budgets_user_category_month",
                columnNames = {
                        "user_id",
                        "category_id",
                        "month"
                }
        )
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "month", nullable = false)
    private LocalDate month;

    @Column(
            name = "limit_amount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal limitAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Budget() {
    }

    public Budget(
            User user,
            Category category,
            LocalDate month,
            BigDecimal limitAmount
    ) {
        this.user = user;
        this.category = category;
        this.month = month;
        this.limitAmount = limitAmount;
    }

    @PrePersist
    private void onCreate() {
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

    public LocalDate getMonth() {
        return month;
    }

    public BigDecimal getLimitAmount() {
        return limitAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
