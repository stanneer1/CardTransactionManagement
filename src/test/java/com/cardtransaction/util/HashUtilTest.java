package com.cardtransaction.util;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HashUtil Tests")
class HashUtilTest {

    private PurchaseTransactionRequest request1;
    private PurchaseTransactionRequest request2;
    private PurchaseTransactionRequest request3;

    @BeforeEach
    void setUp() {
        request1 = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        request2 = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        request3 = PurchaseTransactionRequest.builder()
                .description("Phone Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("799.99"))
                .build();
    }

    @Test
    @DisplayName("Should generate SHA-256 hash for valid request")
    void testGenerateHash_ValidRequest() {
        String hash = HashUtil.generateHash(request1);

        assertNotNull(hash);
        assertFalse(hash.isBlank());
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should generate same hash for identical requests")
    void testGenerateHash_IdenticalRequests() {
        String hash1 = HashUtil.generateHash(request1);
        String hash2 = HashUtil.generateHash(request2);

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Should generate different hashes for different requests")
    void testGenerateHash_DifferentRequests() {
        String hash1 = HashUtil.generateHash(request1);
        String hash3 = HashUtil.generateHash(request3);

        assertNotEquals(hash1, hash3);
    }

    @Test
    @DisplayName("Should generate hash with only hexadecimal characters")
    void testGenerateHash_HexadecimalFormat() {
        String hash = HashUtil.generateHash(request1);

        assertTrue(hash.matches("[0-9a-f]{64}"));
    }

    @Test
    @DisplayName("Should generate different hash when description changes")
    void testGenerateHash_DescriptionChange() {
        String hash1 = HashUtil.generateHash(request1);

        PurchaseTransactionRequest request4 = PurchaseTransactionRequest.builder()
                .description("Different Description")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        String hash4 = HashUtil.generateHash(request4);

        assertNotEquals(hash1, hash4);
    }

    @Test
    @DisplayName("Should generate different hash when date changes")
    void testGenerateHash_DateChange() {
        String hash1 = HashUtil.generateHash(request1);

        PurchaseTransactionRequest request5 = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 11))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        String hash5 = HashUtil.generateHash(request5);

        assertNotEquals(hash1, hash5);
    }

    @Test
    @DisplayName("Should generate different hash when amount changes")
    void testGenerateHash_AmountChange() {
        String hash1 = HashUtil.generateHash(request1);

        PurchaseTransactionRequest request6 = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1200.00"))
                .build();

        String hash6 = HashUtil.generateHash(request6);

        assertNotEquals(hash1, hash6);
    }

    @Test
    @DisplayName("Should consistently generate same hash across multiple calls")
    void testGenerateHash_Consistency() {
        String hash1 = HashUtil.generateHash(request1);
        String hash2 = HashUtil.generateHash(request1);
        String hash3 = HashUtil.generateHash(request1);

        assertEquals(hash1, hash2);
        assertEquals(hash2, hash3);
    }

    @Test
    @DisplayName("Should generate 64-character lowercase hex string")
    void testGenerateHash_FormatValidation() {
        String hash = HashUtil.generateHash(request1);

        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"), "Hash should be lowercase hexadecimal");
    }

    @Test
    @DisplayName("Should handle edge case with minimum amount")
    void testGenerateHash_MinimumAmount() {
        PurchaseTransactionRequest minRequest = PurchaseTransactionRequest.builder()
                .description("Minimum")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("0.01"))
                .build();

        String hash = HashUtil.generateHash(minRequest);

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should handle edge case with large amount")
    void testGenerateHash_LargeAmount() {
        PurchaseTransactionRequest largeRequest = PurchaseTransactionRequest.builder()
                .description("Large")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("9999999999.99"))
                .build();

        String hash = HashUtil.generateHash(largeRequest);

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }
}

