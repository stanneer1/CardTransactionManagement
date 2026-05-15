package com.cardtransaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseTransactionRequest {

    @NotBlank(message = "Description is required")
    @Size(max = 50, message = "Description must not exceed 50 characters")
    private String description;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date must not be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    @NotNull(message = "Purchase amount is required")
    @DecimalMin(value = "0.01", message = "Purchase amount must be a valid positive amount")
    @Digits(integer = 10, fraction = 2, message = "Purchase amount must be rounded to the nearest cent")
    private BigDecimal purchaseAmount;

    // Constructors
    public PurchaseTransactionRequest() {}

    public PurchaseTransactionRequest(String description, LocalDate transactionDate, BigDecimal purchaseAmount) {
        this.description = description;
        this.transactionDate = transactionDate;
        this.purchaseAmount = purchaseAmount;
    }

    // Getters and Setters
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

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String description;
        private LocalDate transactionDate;
        private BigDecimal purchaseAmount;

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

        public PurchaseTransactionRequest build() {
            PurchaseTransactionRequest request = new PurchaseTransactionRequest();
            request.description = this.description;
            request.transactionDate = this.transactionDate;
            request.purchaseAmount = this.purchaseAmount;
            return request;
        }
    }
}

