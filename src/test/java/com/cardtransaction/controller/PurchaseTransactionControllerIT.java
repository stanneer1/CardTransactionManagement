package com.cardtransaction.controller;

import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("PurchaseTransactionController Integration Tests")
class PurchaseTransactionControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PurchaseTransactionRepository repository;

    private String validTransactionJson;

    @BeforeEach
    void setUp() throws Exception {
        repository.deleteAll();

        String payload = """
                {
                    "description": "Laptop Purchase",
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": 1299.99
                }
                """;
        validTransactionJson = payload;
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should create a new purchase transaction with valid data")
    void testCreateTransaction_Success() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Laptop Purchase"))
                .andExpect(jsonPath("$.transactionDate").value("2024-05-10"))
                .andExpect(jsonPath("$.purchaseAmount").value(1299.99));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when no authentication is provided")
    void testCreateTransaction_Unauthorized() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should accept request with basic authentication")
    void testCreateTransaction_WithBasicAuth() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson)
                .with(httpBasic("admin", "admin123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Laptop Purchase"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request for description exceeding 50 characters")
    void testCreateTransaction_DescriptionTooLong() throws Exception {
        String invalidPayload = """
                {
                    "description": "This is a very long description that exceeds the maximum allowed length of 50 characters",
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": 1299.99
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("must not exceed 50 characters"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request for missing description")
    void testCreateTransaction_MissingDescription() throws Exception {
        String invalidPayload = """
                {
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": 1299.99
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("Description is required"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request for negative purchase amount")
    void testCreateTransaction_NegativeAmount() throws Exception {
        String invalidPayload = """
                {
                    "description": "Test",
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": -100.00
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasItem(containsString("valid positive amount"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request for zero purchase amount")
    void testCreateTransaction_ZeroAmount() throws Exception {
        String invalidPayload = """
                {
                    "description": "Test",
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": 0.00
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return bad request for future transaction date")
    void testCreateTransaction_FutureDate() throws Exception {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        String invalidPayload = String.format("""
                {
                    "description": "Test",
                    "transactionDate": "%s",
                    "purchaseAmount": 100.00
                }
                """, futureDate);

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should retrieve all transactions")
    void testGetAllTransactions_Success() throws Exception {
        // Create a few transactions
        PurchaseTransaction t1 = PurchaseTransaction.builder()
                .description("Transaction 1")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("100.00"))
                .build();

        PurchaseTransaction t2 = PurchaseTransaction.builder()
                .description("Transaction 2")
                .transactionDate(LocalDate.of(2024, 5, 11))
                .purchaseAmount(new BigDecimal("200.00"))
                .build();

        repository.save(t1);
        repository.save(t2);

        mockMvc.perform(get("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].description").value("Transaction 1"))
                .andExpect(jsonPath("$[1].description").value("Transaction 2"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should retrieve a transaction by ID")
    void testGetTransactionById_Success() throws Exception {
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description("Test Transaction")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("500.00"))
                .build();

        Long id = repository.save(transaction).getId();

        mockMvc.perform(get("/v1/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Test Transaction"))
                .andExpect(jsonPath("$.purchaseAmount").value(500.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should return 404 when transaction not found")
    void testGetTransactionById_NotFound() throws Exception {
        mockMvc.perform(get("/v1/transactions/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should update a transaction")
    void testUpdateTransaction_Success() throws Exception {
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description("Original")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("100.00"))
                .build();

        Long id = repository.save(transaction).getId();

        String updatePayload = """
                {
                    "description": "Updated",
                    "transactionDate": "2024-05-15",
                    "purchaseAmount": 150.00
                }
                """;

        mockMvc.perform(put("/v1/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.purchaseAmount").value(150.00));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should delete a transaction")
    void testDeleteTransaction_Success() throws Exception {
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description("To Delete")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("100.00"))
                .build();

        Long id = repository.save(transaction).getId();

        mockMvc.perform(delete("/v1/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/transactions/{id}", id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("Should accept requests from USER role")
    void testWithUserRole() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Should validate amount with cents precision")
    void testCreateTransaction_CentsPrecision() throws Exception {
        String payloadWithCents = """
                {
                    "description": "Precise Amount",
                    "transactionDate": "2024-05-10",
                    "purchaseAmount": 1299.99
                }
                """;

        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadWithCents))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchaseAmount").value(1299.99));
    }

    @Test
    @DisplayName("Should reject request with invalid basic auth credentials")
    void testCreateTransaction_InvalidBasicAuth() throws Exception {
        mockMvc.perform(post("/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validTransactionJson)
                .with(httpBasic("admin", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }
}

