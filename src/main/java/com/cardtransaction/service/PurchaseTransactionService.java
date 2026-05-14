package com.cardtransaction.service;

import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.cardtransaction.dto.PurchaseTransactionResponse;
import com.cardtransaction.entity.PurchaseTransaction;
import com.cardtransaction.exception.ResourceNotFoundException;
import com.cardtransaction.repository.PurchaseTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseTransactionService {

    private final PurchaseTransactionRepository repository;

    public PurchaseTransactionService(PurchaseTransactionRepository repository) {
        this.repository = repository;
    }

    /**
     * Create and store a new purchase transaction
     * @param request the purchase transaction request containing description, date, and amount
     * @return the created purchase transaction response
     */
    @Transactional
    public PurchaseTransactionResponse createTransaction(PurchaseTransactionRequest request) {
        PurchaseTransaction transaction = PurchaseTransaction.builder()
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate())
                .purchaseAmount(request.getPurchaseAmount())
                .build();

        PurchaseTransaction savedTransaction = repository.save(transaction);
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
     */
    @Transactional
    public PurchaseTransactionResponse updateTransaction(Long id, PurchaseTransactionRequest request) {
        PurchaseTransaction transaction = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Purchase transaction not found with id: " + id));

        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setPurchaseAmount(request.getPurchaseAmount());

        PurchaseTransaction updatedTransaction = repository.save(transaction);
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
                .build();
    }
}

