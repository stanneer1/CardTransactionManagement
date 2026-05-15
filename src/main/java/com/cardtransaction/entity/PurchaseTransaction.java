package com.cardtransaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "purchase_transactions")
public class PurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String description;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date must not be in the future")
    @Column(nullable = false)
    private LocalDate transactionDate;

    @NotNull(message = "Purchase amount is required")
    @DecimalMin(value = "0.01", message = "Purchase amount must be a valid positive amount")
    @Digits(integer = 10, fraction = 2, message = "Purchase amount must be rounded to the nearest cent")
    @Column(nullable = false, columnDefinition = "DECIMAL(12, 2)")
    private BigDecimal purchaseAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "hash_value", nullable = false, unique = true, length = 64)
    private String hashValue;

    // Constructors
    public PurchaseTransaction() {}

    public PurchaseTransaction(String description, LocalDate transactionDate, BigDecimal purchaseAmount) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmount = purchaseAmount;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(BigDecimal purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String description;
        private LocalDate transactionDate;
        private BigDecimal purchaseAmount;
        private LocalDate createdAt;
        private String hashValue;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder transactionDate(LocalDate transactionDate) {
            this.transactionDate = transactionDate;
            return this;
        }

        public Builder purchaseAmount(BigDecimal purchaseAmount) {
            this.purchaseAmount = purchaseAmount;
            return this;
        }

        public Builder createdAt(LocalDate createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder hashValue(String hashValue) {
            this.hashValue = hashValue;
            return this;
        }

        public PurchaseTransaction build() {
            PurchaseTransaction transaction = new PurchaseTransaction();
            transaction.id = this.id;
            transaction.description = this.description;
            transaction.transactionDate = this.transactionDate;
            transaction.purchaseAmount = this.purchaseAmount;
            transaction.createdAt = this.createdAt;
            transaction.hashValue = this.hashValue;
            return transaction;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }

    @Override
    public String toString() {
        return "PurchaseTransaction{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", transactionDate=" + transactionDate +
                ", purchaseAmount=" + purchaseAmount +
                ", createdAt=" + createdAt +
                ", hashValue='" + hashValue + '\'' +
                '}';
    }
}


