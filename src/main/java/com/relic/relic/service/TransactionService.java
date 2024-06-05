package com.relic.relic.service;

import com.relic.relic.model.Transaction;

import java.util.List;

public interface TransactionService {
    Transaction createCustomerPaymentTransaction(Long orderId, Long customerId, Double amount);
    Transaction createVendorDisbursementTransaction(Long orderId, Long vendorId, Double amount);
    Transaction getTransactionById(Long id);
    List<Transaction> getTransactionsByUser(Long userId);
    List<Transaction> getTransactionsByOrder(Long orderId);
    List<Transaction> getAllTransactions(
            Long userId,
            Long orderId,
            String startDate,
            String endDate,
            String transactionType,
            String sortBy
    );
    void deleteTransaction(Long id);
}
