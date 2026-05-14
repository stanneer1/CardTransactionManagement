package com.cardtransaction.controller;

import com.cardtransaction.dto.ConvertedPurchaseResponse;
import com.cardtransaction.dto.PurchaseTransactionRequest;
import com.cardtransaction.dto.PurchaseTransactionResponse;
import com.cardtransaction.service.CurrencyConversionService;
import com.cardtransaction.service.PurchaseTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/transactions")
public class PurchaseTransactionController {

    private final PurchaseTransactionService transactionService;
    private final CurrencyConversionService currencyConversionService;

    public PurchaseTransactionController(PurchaseTransactionService transactionService,
                                        CurrencyConversionService currencyConversionService) {
        this.transactionService = transactionService;
        this.currencyConversionService = currencyConversionService;
    }

    /**
     * Create a new purchase transaction
     * POST /api/v1/transactions
     * @param request the purchase transaction request
     * @return the created purchase transaction
     */
    @PostMapping
    public ResponseEntity<PurchaseTransactionResponse> createTransaction(
            @Valid @RequestBody PurchaseTransactionRequest request) {
        PurchaseTransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieve all purchase transactions
     * GET /api/v1/transactions
     * @return list of all purchase transactions
     */
    @GetMapping
    public ResponseEntity<List<PurchaseTransactionResponse>> getAllTransactions() {
        List<PurchaseTransactionResponse> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Retrieve a specific purchase transaction by ID
     * GET /api/v1/transactions/{id}
     * @param id the transaction ID
     * @return the purchase transaction
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseTransactionResponse> getTransactionById(
            @PathVariable Long id) {
        PurchaseTransactionResponse response = transactionService.getTransactionById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a purchase transaction
     * PUT /api/v1/transactions/{id}
     * @param id the transaction ID
     * @param request the updated purchase transaction request
     * @return the updated purchase transaction
     */
    @PutMapping("/{id}")
    public ResponseEntity<PurchaseTransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseTransactionRequest request) {
        PurchaseTransactionResponse response = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a purchase transaction
     * DELETE /api/v1/transactions/{id}
     * @param id the transaction ID
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert a purchase transaction to a target currency
     * GET /api/v1/transactions/{id}/convert?currency=EUR
     * @param id the transaction ID
     * @param currency the target currency code
     * @return the converted purchase transaction
     */
    @GetMapping("/{id}/convert")
    public ResponseEntity<ConvertedPurchaseResponse> convertTransactionToCurrency(
            @PathVariable Long id,
            @RequestParam String currency) {
        var transaction = transactionService.getTransactionEntityById(id);
        ConvertedPurchaseResponse response = currencyConversionService.convertPurchase(transaction, currency);
        return ResponseEntity.ok(response);
    }
}

