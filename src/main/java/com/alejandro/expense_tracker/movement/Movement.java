package com.alejandro.expense_tracker.movement;

import com.alejandro.expense_tracker.category.Category;
import com.alejandro.expense_tracker.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "movements")
public class Movement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "movement_date", nullable = false)
    private LocalDate movementDate;

    @Column(name = "description")
    private String descripcion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Movement() {

    }

    public Movement(User user, Category category, MovementType type, BigDecimal amount, LocalDate movementDate, String descripcion) {
        this.user = user;
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.movementDate = movementDate;
        this.descripcion = descripcion;
    }

    @PrePersist
    private void onCreated() {
        this.createdAt = Instant.now();
    }
    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getMovementDate() {
        return movementDate;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void updateDetails(
            Category category,
            MovementType type,
            BigDecimal amount,
            LocalDate movementDate,
            String descripcion
    ) {
        this.category = category;
        this.type = type;
        this.amount = amount;
        this.movementDate = movementDate;
        this.descripcion = descripcion;
    }
}
