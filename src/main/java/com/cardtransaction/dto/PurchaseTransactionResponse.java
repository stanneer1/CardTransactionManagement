package com.cardtransaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseTransactionResponse {

    private Long id;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal purchaseAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate createdAt;

    private String hashValue;

    // Constructors
    public PurchaseTransactionResponse() {}

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

        public PurchaseTransactionResponse build() {
            PurchaseTransactionResponse response = new PurchaseTransactionResponse();
            response.id = this.id;
            response.description = this.description;
            response.transactionDate = this.transactionDate;
            response.purchaseAmount = this.purchaseAmount;
            response.createdAt = this.createdAt;
            response.hashValue = this.hashValue;
            return response;
        }
    }
}

