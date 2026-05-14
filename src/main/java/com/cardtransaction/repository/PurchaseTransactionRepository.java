package com.cardtransaction.repository;

import com.cardtransaction.entity.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, Long> {
    List<PurchaseTransaction> findByTransactionDateBefore(LocalDate date);
    List<PurchaseTransaction> findByTransactionDateAfter(LocalDate date);
    List<PurchaseTransaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
}

