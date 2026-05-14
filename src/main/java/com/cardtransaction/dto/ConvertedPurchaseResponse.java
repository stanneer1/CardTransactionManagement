package com.cardtransaction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ConvertedPurchaseResponse {

    private Long id;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;

    private BigDecimal originalPurchaseAmount;

    private String originalCurrency;

    private String targetCurrency;

    private BigDecimal exchangeRate;

    private BigDecimal convertedAmount;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate exchangeRateDate;

    // Constructors
    public ConvertedPurchaseResponse() {}

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

    public BigDecimal getOriginalPurchaseAmount() {
        return originalPurchaseAmount;
    }

    public void setOriginalPurchaseAmount(BigDecimal originalPurchaseAmount) {
        this.originalPurchaseAmount = originalPurchaseAmount;
    }

    public String getOriginalCurrency() {
        return originalCurrency;
    }

    public void setOriginalCurrency(String originalCurrency) {
        this.originalCurrency = originalCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public BigDecimal getConvertedAmount() {
        return convertedAmount;
    }

    public void setConvertedAmount(BigDecimal convertedAmount) {
        this.convertedAmount = convertedAmount;
    }

    public LocalDate getExchangeRateDate() {
        return exchangeRateDate;
    }

    public void setExchangeRateDate(LocalDate exchangeRateDate) {
        this.exchangeRateDate = exchangeRateDate;
    }

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String description;
        private LocalDate transactionDate;
        private BigDecimal originalPurchaseAmount;
        private String originalCurrency;
        private String targetCurrency;
        private BigDecimal exchangeRate;
        private BigDecimal convertedAmount;
        private LocalDate exchangeRateDate;

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

        public Builder originalPurchaseAmount(BigDecimal originalPurchaseAmount) {
            this.originalPurchaseAmount = originalPurchaseAmount;
            return this;
        }

        public Builder originalCurrency(String originalCurrency) {
            this.originalCurrency = originalCurrency;
            return this;
        }

        public Builder targetCurrency(String targetCurrency) {
            this.targetCurrency = targetCurrency;
            return this;
        }

        public Builder exchangeRate(BigDecimal exchangeRate) {
            this.exchangeRate = exchangeRate;
            return this;
        }

        public Builder convertedAmount(BigDecimal convertedAmount) {
            this.convertedAmount = convertedAmount;
            return this;
        }

        public Builder exchangeRateDate(LocalDate exchangeRateDate) {
            this.exchangeRateDate = exchangeRateDate;
            return this;
        }

        public ConvertedPurchaseResponse build() {
            ConvertedPurchaseResponse response = new ConvertedPurchaseResponse();
            response.id = this.id;
            response.description = this.description;
            response.transactionDate = this.transactionDate;
            response.originalPurchaseAmount = this.originalPurchaseAmount;
            response.originalCurrency = this.originalCurrency;
            response.targetCurrency = this.targetCurrency;
            response.exchangeRate = this.exchangeRate;
            response.convertedAmount = this.convertedAmount;
            response.exchangeRateDate = this.exchangeRateDate;
            return response;
        }
    }
}

