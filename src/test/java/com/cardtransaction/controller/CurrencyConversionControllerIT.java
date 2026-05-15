package com.cardtransaction.controller;

import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import com.cardtransaction.util.HashUtil;
import com.cardtransaction.dto.PurchaseTransactionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Currency Conversion Endpoint Integration Tests")
class CurrencyConversionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PurchaseTransactionRepository repository;

    @MockBean
    private RestOperations restTemplate;

    private Long transactionId;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        PurchaseTransactionRequest request = PurchaseTransactionRequest.builder()
                .description("Test Product")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1000.00"))
                .build();

        String hashValue = HashUtil.generateHash(request);

        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description("Test Product")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1000.00"))
                .hashValue(hashValue)
                .build();

        transactionId = repository.save(transaction).getId();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should convert transaction to EUR successfully")
    void testConvertTransaction_ToEUR_Success() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"0.92\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.originalPurchaseAmount").value(1000.00))
                .andExpect(jsonPath("$.originalCurrency").value("USD"))
                .andExpect(jsonPath("$.targetCurrency").value("EUR"))
                .andExpect(jsonPath("$.exchangeRate").value(0.92))
                .andExpect(jsonPath("$.convertedAmount").value(920.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should convert transaction to GBP successfully")
    void testConvertTransaction_ToGBP_Success() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"0.79\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "GBP")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("GBP"))
                .andExpect(jsonPath("$.exchangeRate").value(0.79))
                .andExpect(jsonPath("$.convertedAmount").value(790.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should convert transaction to JPY successfully")
    void testConvertTransaction_ToJPY_Success() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"110.50\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "JPY")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetCurrency").value("JPY"))
                .andExpect(jsonPath("$.convertedAmount").value(110500.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 422 when no exchange rate is available within 6 months")
    void testConvertTransaction_NoExchangeRateAvailable() throws Exception {
        String apiResponse = "{\"data\": []}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "XYZ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("No currency conversion rate available")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 404 when transaction not found")
    void testConvertTransaction_TransactionNotFound() throws Exception {
        mockMvc.perform(get("/v1/transactions/999/convert")
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @DisplayName("Should return 401 when not authenticated")
    void testConvertTransaction_Unauthorized() throws Exception {
        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should round converted amount to 2 decimal places")
    void testConvertTransaction_Rounding() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"1.23456789\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(1234.57));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Should allow USER role to access conversion endpoint")
    void testConvertTransaction_WithUserRole() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"0.92\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should handle API service error gracefully")
    void testConvertTransaction_ServiceError() throws Exception {
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RestClientException("Service unavailable"));

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Unable to retrieve currency exchange rate")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should include exchange rate date in response")
    void testConvertTransaction_IncludesExchangeRateDate() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"0.92\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeRateDate").value("2024-05-10"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should include transaction description in converted response")
    void testConvertTransaction_IncludesTransactionDescription() throws Exception {
        String apiResponse = "{\"data\": [{\"exchange_rate\": \"0.92\", \"effective_date\": \"2024-05-10\"}]}";
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        mockMvc.perform(get("/v1/transactions/{id}/convert", transactionId)
                .param("currency", "EUR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Test Product"));
    }
}
