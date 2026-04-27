package com.paymentgateway.payment.repository;

import com.paymentgateway.common.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByMerchantId(String merchantId);

    List<Transaction> findByStatus(Transaction.TransactionStatus status);

    List<Transaction> findByMerchantIdAndStatus(String merchantId, Transaction.TransactionStatus status);

    java.util.Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
