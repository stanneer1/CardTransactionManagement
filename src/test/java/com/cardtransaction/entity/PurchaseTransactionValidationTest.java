package com.cardtransaction.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("PurchaseTransaction Entity Validation Tests")
class PurchaseTransactionValidationTest {

    @Autowired
    private Validator validator;

    private PurchaseTransaction validTransaction;

    @BeforeEach
    void setUp() {
        validTransaction = PurchaseTransaction.builder()
                .description("Valid Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Should pass validation with valid transaction")
    void testValidTransaction() {
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when description is blank")
    void testBlankDescription() {
        validTransaction.setDescription("");
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Description is required")));
    }

    @Test
    @DisplayName("Should fail validation when description is null")
    void testNullDescription() {
        validTransaction.setDescription(null);
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when description exceeds 50 characters")
    void testDescriptionTooLong() {
        validTransaction.setDescription("This is a description that definitely exceeds 50 characters max");
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not exceed 50 characters")));
    }

    @Test
    @DisplayName("Should pass validation with exactly 50 character description")
    void testMaxLengthDescription() {
        validTransaction.setDescription("x".repeat(50));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when transaction date is null")
    void testNullTransactionDate() {
        validTransaction.setTransactionDate(null);
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when transaction date is in the future")
    void testFutureTransactionDate() {
        validTransaction.setTransactionDate(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("must not be in the future")));
    }

    @Test
    @DisplayName("Should pass validation with today's date")
    void testTodayTransactionDate() {
        validTransaction.setTransactionDate(LocalDate.now());
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with past date")
    void testPastTransactionDate() {
        validTransaction.setTransactionDate(LocalDate.now().minusDays(1));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when purchase amount is null")
    void testNullPurchaseAmount() {
        validTransaction.setPurchaseAmount(null);
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when purchase amount is negative")
    void testNegativePurchaseAmount() {
        validTransaction.setPurchaseAmount(new BigDecimal("-100.00"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("valid positive amount")));
    }

    @Test
    @DisplayName("Should fail validation when purchase amount is zero")
    void testZeroPurchaseAmount() {
        validTransaction.setPurchaseAmount(new BigDecimal("0.00"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with valid purchase amount")
    void testValidPurchaseAmount() {
        validTransaction.setPurchaseAmount(new BigDecimal("0.01"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should validate amount with cents precision")
    void testAmountWithCentsFormat() {
        validTransaction.setPurchaseAmount(new BigDecimal("1234.56"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should pass validation with large purchase amount")
    void testLargePurchaseAmount() {
        validTransaction.setPurchaseAmount(new BigDecimal("999999999.99"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation with amount having more than 2 decimal places")
    void testAmountWithExcessiveDecimalPlaces() {
        validTransaction.setPurchaseAmount(new BigDecimal("100.999"));
        Set<ConstraintViolation<PurchaseTransaction>> violations = validator.validate(validTransaction);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("nearest cent")));
    }
}

