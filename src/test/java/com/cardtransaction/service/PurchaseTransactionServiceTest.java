package com.cardtransaction.service;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.cardtransaction.dto.PurchaseTransactionResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.ResourceNotFoundException;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurchaseTransactionService Tests")
class PurchaseTransactionServiceTest {

    @Mock
    private PurchaseTransactionRepository repository;

    @InjectMocks
    private PurchaseTransactionService service;

    private PurchaseTransactionRequest testRequest;
    private PurchaseTransaction testTransaction;

    @BeforeEach
    void setUp() {
        testRequest = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        testTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create a new purchase transaction successfully")
    void testCreateTransaction_Success() {
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(testTransaction);

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertNotNull(response);
        assertEquals("Laptop Purchase", response.getDescription());
        assertEquals(LocalDate.of(2024, 5, 10), response.getTransactionDate());
        assertEquals(new BigDecimal("1299.99"), response.getPurchaseAmount());
        verify(repository, times(1)).save(any(PurchaseTransaction.class));
    }

    @Test
    @DisplayName("Should retrieve a transaction by ID successfully")
    void testGetTransactionById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));

        PurchaseTransactionResponse response = service.getTransactionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop Purchase", response.getDescription());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found")
    void testGetTransactionById_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getTransactionById(999L));
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should retrieve all transactions successfully")
    void testGetAllTransactions_Success() {
        List<PurchaseTransaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);

        PurchaseTransaction transaction2 = PurchaseTransaction.builder()
                .id(2L)
                .description("Phone Purchase")
                .transactionDate(LocalDate.of(2024, 5, 12))
                .purchaseAmount(new BigDecimal("799.99"))
                .createdAt(LocalDate.now())
                .build();
        transactions.add(transaction2);

        when(repository.findAll()).thenReturn(transactions);

        List<PurchaseTransactionResponse> responses = service.getAllTransactions();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Laptop Purchase", responses.get(0).getDescription());
        assertEquals("Phone Purchase", responses.get(1).getDescription());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no transactions exist")
    void testGetAllTransactions_Empty() {
        when(repository.findAll()).thenReturn(new ArrayList<>());

        List<PurchaseTransactionResponse> responses = service.getAllTransactions();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update a transaction successfully")
    void testUpdateTransaction_Success() {
        PurchaseTransactionRequest updateRequest = PurchaseTransactionRequest.builder()
                .description("Updated Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 15))
                .purchaseAmount(new BigDecimal("1399.99"))
                .build();

        PurchaseTransaction updatedTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description("Updated Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 15))
                .purchaseAmount(new BigDecimal("1399.99"))
                .createdAt(LocalDate.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(updatedTransaction);

        PurchaseTransactionResponse response = service.updateTransaction(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Laptop Purchase", response.getDescription());
        assertEquals(new BigDecimal("1399.99"), response.getPurchaseAmount());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(PurchaseTransaction.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent transaction")
    void testUpdateTransaction_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateTransaction(999L, testRequest));
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should delete a transaction successfully")
    void testDeleteTransaction_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));
        doNothing().when(repository).delete(any(PurchaseTransaction.class));

        service.deleteTransaction(1L);

        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).delete(testTransaction);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent transaction")
    void testDeleteTransaction_NotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteTransaction(999L));
        verify(repository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should get transaction entity by ID successfully")
    void testGetTransactionEntityById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));

        PurchaseTransaction transaction = service.getTransactionEntityById(1L);

        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals("Laptop Purchase", transaction.getDescription());
        verify(repository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should validate description field parsing in response")
    void testCreateTransaction_ValidateDescriptionMapping() {
        String description = "Office Supplies Purchase";
        testRequest.setDescription(description);

        when(repository.save(any(PurchaseTransaction.class))).thenReturn(
                PurchaseTransaction.builder()
                        .id(1L)
                        .description(description)
                        .transactionDate(testRequest.getTransactionDate())
                        .purchaseAmount(testRequest.getPurchaseAmount())
                        .createdAt(LocalDate.now())
                        .build()
        );

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertEquals(description, response.getDescription());
    }

    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    void testCreateTransaction_ValidateBigDecimalPrecision() {
        BigDecimal amount = new BigDecimal("1299.99");
        testRequest.setPurchaseAmount(amount);

        when(repository.save(any(PurchaseTransaction.class))).thenReturn(
                PurchaseTransaction.builder()
                        .id(1L)
                        .description(testRequest.getDescription())
                        .transactionDate(testRequest.getTransactionDate())
                        .purchaseAmount(amount)
                        .createdAt(LocalDate.now())
                        .build()
        );

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertEquals(0, amount.compareTo(response.getPurchaseAmount()));
    }
}

