package com.b2b.ordermanagement.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "partners")
public class Partner {

    @Id
    private String id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @PositiveOrZero
    @Column(name = "credit_limit", nullable = false, precision = 12, scale = 2)
    private BigDecimal creditLimit;

    @NotNull
    @PositiveOrZero
    @Column(name = "available_credit", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableCredit;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    protected Partner() {}

    public Partner(String id, String name, BigDecimal creditLimit) {
        this.id = id;
        this.name = name;
        this.creditLimit = creditLimit;
        this.availableCredit = creditLimit;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean hasAvailableCredit(BigDecimal amount) {
        return availableCredit.compareTo(amount) >= 0;
    }

    public void debitCredit(BigDecimal amount) {
        if (!hasAvailableCredit(amount)) {
            throw new IllegalArgumentException("Insufficient credit available");
        }
        this.availableCredit = this.availableCredit.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void creditCredit(BigDecimal amount) {
        this.availableCredit = this.availableCredit.add(amount);
        if (this.availableCredit.compareTo(this.creditLimit) > 0) {
            this.availableCredit = this.creditLimit;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public void setAvailableCredit(BigDecimal availableCredit) {
        this.availableCredit = availableCredit;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partner partner)) return false;
        return Objects.equals(id, partner.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}