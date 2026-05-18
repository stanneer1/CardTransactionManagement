package com.cardtransaction.service;

import com.cardtransaction.dto.ConvertedPurchaseResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.CurrencyConversionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrencyConversionService Unit Tests")
class CurrencyConversionServiceTest {

    @Mock
    private RestOperations restTemplate;

    @Mock
    private com.cardtransaction.config.TreasuryProperties treasuryProperties;

    @InjectMocks
    private CurrencyConversionService service;

    private PurchaseTransaction testTransaction;

    @BeforeEach
    void setUp() {
        // Provide sensible defaults for currency mapping used by the service
        org.mockito.Mockito.lenient().when(treasuryProperties.mapIsoToTreasuryCurrency(anyString())).thenAnswer(invocation -> {
            String arg = invocation.getArgument(0, String.class);
            if (arg == null) return "";
            switch (arg.trim().toUpperCase()) {
                case "USD": return "Dollar";
                case "EUR": return "Euro";
                case "INR": return "Rupee";
                default: return arg;
            }
        });
        testTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description("Electronics Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1000.00"))
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should throw exception when source and target currencies are the same")
    void testConvertPurchase_SameCurrency() {
        assertThrows(CurrencyConversionException.class, () ->
                service.convertPurchase(testTransaction, "USD", "USD")
        );
    }

    @Test
    @DisplayName("Should throw exception when transaction is null")
    void testConvertPurchase_NullTransaction() {
        assertThrows(NullPointerException.class, () ->
                service.convertPurchase(null, "USD", "EUR")
        );
    }

    @Test
    @DisplayName("Should validate currency codes are not empty")
    void testConvertPurchase_EmptyCurrencyCode() {
        assertThrows(CurrencyConversionException.class, () ->
                service.convertPurchase(testTransaction, "", "EUR")
        );
    }

    @Test
    @DisplayName("Should handle API connection errors gracefully")
    void testConvertPurchase_ApiConnectionError() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Connection timeout"));

        assertThrows(CurrencyConversionException.class, () ->
                service.convertPurchase(testTransaction, "EUR")
        );
    }

    @Test
    @DisplayName("Should throw exception for different source and target")
    void testConvertPurchase_DifferentCurrencies() {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("{\"data\": []}");

        assertThrows(CurrencyConversionException.class, () ->
                service.convertPurchase(testTransaction, "USD", "EUR")
        );
    }
}
