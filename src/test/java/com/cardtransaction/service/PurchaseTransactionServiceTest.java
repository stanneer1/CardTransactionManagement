package com.cardtransaction.service;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.cardtransaction.dto.PurchaseTransactionResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.DuplicateTransactionException;
import com.cardtransaction.exception.ResourceNotFoundException;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import com.cardtransaction.util.HashUtil;
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
    private String testHash;

    @BeforeEach
    void setUp() {
        testRequest = PurchaseTransactionRequest.builder()
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .build();

        testHash = HashUtil.generateHash(testRequest);

        testTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description("Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 10))
                .purchaseAmount(new BigDecimal("1299.99"))
                .hashValue(testHash)
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create a new purchase transaction successfully")
    void testCreateTransaction_Success() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.empty());
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(testTransaction);

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertNotNull(response);
        assertEquals("Laptop Purchase", response.getDescription());
        assertEquals(LocalDate.of(2024, 5, 10), response.getTransactionDate());
        assertEquals(new BigDecimal("1299.99"), response.getPurchaseAmount());
        assertEquals(testHash, response.getHashValue());
        verify(repository, times(1)).findByHashValue(testHash);
        verify(repository, times(1)).save(any(PurchaseTransaction.class));
    }

    @Test
    @DisplayName("Should throw DuplicateTransactionException when trying to create duplicate")
    void testCreateTransaction_Duplicate() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.of(testTransaction));

        assertThrows(DuplicateTransactionException.class, () -> service.createTransaction(testRequest));
        verify(repository, times(1)).findByHashValue(testHash);
        verify(repository, never()).save(any(PurchaseTransaction.class));
    }

    @Test
    @DisplayName("Should retrieve a transaction by ID successfully")
    void testGetTransactionById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));

        PurchaseTransactionResponse response = service.getTransactionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Laptop Purchase", response.getDescription());
        assertEquals(testHash, response.getHashValue());
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

        String hash2 = HashUtil.generateHash(PurchaseTransactionRequest.builder()
                .description("Phone Purchase")
                .transactionDate(LocalDate.of(2024, 5, 12))
                .purchaseAmount(new BigDecimal("799.99"))
                .build());

        PurchaseTransaction transaction2 = PurchaseTransaction.builder()
                .id(2L)
                .description("Phone Purchase")
                .transactionDate(LocalDate.of(2024, 5, 12))
                .purchaseAmount(new BigDecimal("799.99"))
                .hashValue(hash2)
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

        String newHash = HashUtil.generateHash(updateRequest);

        PurchaseTransaction updatedTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description("Updated Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 15))
                .purchaseAmount(new BigDecimal("1399.99"))
                .hashValue(newHash)
                .createdAt(LocalDate.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(repository.findByHashValue(newHash)).thenReturn(Optional.empty());
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(updatedTransaction);

        PurchaseTransactionResponse response = service.updateTransaction(1L, updateRequest);

        assertNotNull(response);
        assertEquals("Updated Laptop Purchase", response.getDescription());
        assertEquals(new BigDecimal("1399.99"), response.getPurchaseAmount());
        assertEquals(newHash, response.getHashValue());
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).findByHashValue(newHash);
        verify(repository, times(1)).save(any(PurchaseTransaction.class));
    }

    @Test
    @DisplayName("Should throw DuplicateTransactionException when updating to duplicate hash")
    void testUpdateTransaction_DuplicateHash() {
        PurchaseTransactionRequest updateRequest = PurchaseTransactionRequest.builder()
                .description("Updated Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 15))
                .purchaseAmount(new BigDecimal("1399.99"))
                .build();

        String newHash = HashUtil.generateHash(updateRequest);

        PurchaseTransaction existingTransaction = PurchaseTransaction.builder()
                .id(2L)
                .description("Updated Laptop Purchase")
                .transactionDate(LocalDate.of(2024, 5, 15))
                .purchaseAmount(new BigDecimal("1399.99"))
                .hashValue(newHash)
                .createdAt(LocalDate.now())
                .build();

        when(repository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(repository.findByHashValue(newHash)).thenReturn(Optional.of(existingTransaction));

        assertThrows(DuplicateTransactionException.class, () -> service.updateTransaction(1L, updateRequest));
        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).findByHashValue(newHash);
        verify(repository, never()).save(any(PurchaseTransaction.class));
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

        String newHash = HashUtil.generateHash(testRequest);

        PurchaseTransaction savedTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description(description)
                .transactionDate(testRequest.getTransactionDate())
                .purchaseAmount(testRequest.getPurchaseAmount())
                .hashValue(newHash)
                .createdAt(LocalDate.now())
                .build();

        when(repository.findByHashValue(newHash)).thenReturn(Optional.empty());
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(savedTransaction);

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertEquals(description, response.getDescription());
        assertEquals(newHash, response.getHashValue());
    }

    @Test
    @DisplayName("Should handle BigDecimal precision correctly")
    void testCreateTransaction_ValidateBigDecimalPrecision() {
        BigDecimal amount = new BigDecimal("1299.99");
        testRequest.setPurchaseAmount(amount);

        String hash = HashUtil.generateHash(testRequest);

        PurchaseTransaction savedTransaction = PurchaseTransaction.builder()
                .id(1L)
                .description(testRequest.getDescription())
                .transactionDate(testRequest.getTransactionDate())
                .purchaseAmount(amount)
                .hashValue(hash)
                .createdAt(LocalDate.now())
                .build();

        when(repository.findByHashValue(hash)).thenReturn(Optional.empty());
        when(repository.save(any(PurchaseTransaction.class))).thenReturn(savedTransaction);

        PurchaseTransactionResponse response = service.createTransaction(testRequest);

        assertEquals(0, amount.compareTo(response.getPurchaseAmount()));
        assertEquals(hash, response.getHashValue());
    }

    @Test
    @DisplayName("Should check if transaction is duplicate")
    void testIsDuplicateTransaction() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.of(testTransaction));

        boolean isDuplicate = service.isDuplicateTransaction(testHash);

        assertTrue(isDuplicate);
        verify(repository, times(1)).findByHashValue(testHash);
    }

    @Test
    @DisplayName("Should return false when transaction is not duplicate")
    void testIsNotDuplicateTransaction() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.empty());

        boolean isDuplicate = service.isDuplicateTransaction(testHash);

        assertFalse(isDuplicate);
        verify(repository, times(1)).findByHashValue(testHash);
    }

    @Test
    @DisplayName("Should retrieve transaction by hash successfully")
    void testGetTransactionByHash_Success() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.of(testTransaction));

        PurchaseTransactionResponse response = service.getTransactionByHash(testHash);

        assertNotNull(response);
        assertEquals("Laptop Purchase", response.getDescription());
        assertEquals(testHash, response.getHashValue());
        verify(repository, times(1)).findByHashValue(testHash);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when hash not found")
    void testGetTransactionByHash_NotFound() {
        when(repository.findByHashValue(testHash)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getTransactionByHash(testHash));
        verify(repository, times(1)).findByHashValue(testHash);
    }
}

