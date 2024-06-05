package com.relic.relic.controller;

import com.relic.relic.model.Transaction;
import com.relic.relic.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/payment")
    public ResponseEntity<Transaction> createCustomerPaymentTransaction(
            @RequestParam Long orderId,
            @RequestParam Long customerId,
            @RequestParam Double amount) {
        Transaction transaction = transactionService.createCustomerPaymentTransaction(orderId, customerId, amount);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @PostMapping("/disbursement")
    public ResponseEntity<Transaction> createVendorDisbursementTransaction(
            @RequestParam Long orderId,
            @RequestParam Long vendorId,
            @RequestParam Double amount) {
        Transaction transaction = transactionService.createVendorDisbursementTransaction(orderId, vendorId, amount);
        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getTransactionsByUser(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getTransactionsByUser(userId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Transaction>> getTransactionsByOrder(@PathVariable Long orderId) {
        List<Transaction> transactions = transactionService.getTransactionsByOrder(orderId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Transaction>> getAllTransactions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false, defaultValue = "dateDesc") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<Transaction> transactions = transactionService.getAllTransactions(userId, orderId, startDate, endDate, transactionType, sortBy);

        int start = Math.min(page * size, transactions.size());
        int end = Math.min((page + 1) * size, transactions.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = new PageImpl<>(transactions.subList(start, end), pageable, transactions.size());

        return new ResponseEntity<>(transactionPage, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return new ResponseEntity<>("Transaction deleted successfully", HttpStatus.OK);
    }
}
