package com.cardtransaction.service;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.cardtransaction.dto.PurchaseTransactionResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.DuplicateTransactionException;
import com.cardtransaction.exception.ResourceNotFoundException;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import com.cardtransaction.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseTransactionService.class);
    private final PurchaseTransactionRepository repository;

    public PurchaseTransactionService(PurchaseTransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Create and store a new purchase transaction with duplicate detection.
     * Generates a SHA-256 hash of the request to detect duplicate transactions.
     * @param request the purchase transaction request containing description, date, and amount
     * @return the created purchase transaction response
     * @throws DuplicateTransactionException if a transaction with the same hash already exists
     */
    @Transactional
    public PurchaseTransactionResponse createTransaction(PurchaseTransactionRequest request) {
        // Generate hash from request
        String hashValue = HashUtil.generateHash(request);
        logger.info("Creating transaction with hash: {}", hashValue);

        // Check for duplicate transaction
        if (isDuplicateTransaction(hashValue)) {
            logger.warn("Duplicate transaction detected with hash: {}", hashValue);
            throw new DuplicateTransactionException(
                    "A transaction with the same details already exists. Hash: " + hashValue);
        }

        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .purchaseAmount(request.getPurchaseAmount())
                .hashValue(hashValue)
                .build();

        PurchaseTransaction savedTransaction = repository.save(transaction);
        logger.info("Transaction created successfully with ID: {} and hash: {}",
                savedTransaction.getId(), hashValue);
        return convertToResponse(savedTransaction);
    }

    /**
     * Retrieve a purchase transaction by ID
     * @param id the transaction ID
     * @return the purchase transaction response
     * @throws ResourceNotFoundException if transaction not found
     */
    @Transactional(readOnly = true)
    public PurchaseTransactionResponse getTransactionById(Long id) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with id: " + id));
        return convertToResponse(transaction);
    }

    /**
     * Retrieve all purchase transactions
     * @return list of purchase transaction responses
     */
    @Transactional(readOnly = true)
    public List<PurchaseTransactionResponse> getAllTransactions() {
        return repository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing purchase transaction
     * @param id the transaction ID
     * @param request the updated purchase transaction request
     * @return the updated purchase transaction response
     * @throws ResourceNotFoundException if transaction not found
     * @throws DuplicateTransactionException if new hash matches another transaction
     */
    @Transactional
    public PurchaseTransactionResponse updateTransaction(Long id, PurchaseTransactionRequest request) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with id: " + id));

        // Generate new hash
        String newHashValue = HashUtil.generateHash(request);

        // Check if the new hash is different and if it already exists in another transaction
        if (!transaction.getHashValue().equals(newHashValue) && isDuplicateTransaction(newHashValue)) {
            logger.warn("Duplicate transaction detected with hash: {} during update", newHashValue);
            throw new DuplicateTransactionException(
                    "Another transaction with the same details already exists. Hash: " + newHashValue);
        }

        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPurchaseAmount(request.getPurchaseAmount());
        transaction.setHashValue(newHashValue);

        PurchaseTransaction updatedTransaction = repository.save(transaction);
        logger.info("Transaction updated successfully with ID: {} and new hash: {}",
                updatedTransaction.getId(), newHashValue);
        return convertToResponse(updatedTransaction);
    }

    /**
     * Delete a purchase transaction
     * @param id the transaction ID
     * @throws ResourceNotFoundException if transaction not found
     */
    @Transactional
    public void deleteTransaction(Long id) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with id: " + id));
        repository.delete(transaction);
        logger.info("Transaction deleted successfully with ID: {}", id);
    }

    /**
     * Get the underlying PurchaseTransaction entity by ID
     * @param id the transaction ID
     * @return the purchase transaction entity
     * @throws ResourceNotFoundException if transaction not found
     */
    @Transactional(readOnly = true)
    public PurchaseTransaction getTransactionEntityById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with id: " + id));
    }

    /**
     * Check if a transaction with the given hash already exists
     * @param hashValue the hash value to check
     * @return true if a transaction with this hash exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateTransaction(String hashValue) {
        return repository.findByHashValue(hashValue).isPresent();
    }

    /**
     * Get a transaction by its hash value
     * @param hashValue the hash value
     * @return the purchase transaction response if found
     * @throws ResourceNotFoundException if transaction not found
     */
    @Transactional(readOnly = true)
    public PurchaseTransactionResponse getTransactionByHash(String hashValue) {
        PurchaseTransaction transaction = repository.findByHashValue(hashValue)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with hash: " + hashValue));
        return convertToResponse(transaction);
    }

    /**
     * Convert PurchaseTransaction entity to response DTO
     * @param transaction the purchase transaction entity
     * @return the purchase transaction response
     */
    private PurchaseTransactionResponse convertToResponse(PurchaseTransaction transaction) {
        return PurchaseTransactionResponse.builder()
                .id(transaction.getId())
                .description(transaction.getDescription())
                .transactionDate(transaction.getTransactionDate())
                .purchaseAmount(transaction.getPurchaseAmount())
                .createdAt(transaction.getCreatedAt())
                .hashValue(transaction.getHashValue())
                .build();
    }
}

