package com.relic.relic.service.implementations;

import com.relic.relic.enums.TransactionType;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.Order;
import com.relic.relic.model.Transaction;
import com.relic.relic.model.User;
import com.relic.relic.repository.OrderRepository;
import com.relic.relic.repository.TransactionRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.TransactionService;
import com.relic.relic.util.UserAuthenticationHelper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class TransactionServiceImple implements TransactionService {
    private ModelMapper modelMapper;
    private UserAuthenticationHelper userAuthenticationHelper;
    private final TransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    public Transaction createCustomerPaymentTransaction(Long orderId, Long customerId, Double amount) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }

        Optional<User> user = userRepository.findById(customerId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Transaction transaction = new Transaction();
        transaction.setOrder(order.get());
        transaction.setUser(user.get());
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.PAYMENT);
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction createVendorDisbursementTransaction(Long orderId, Long vendorId, Double amount) {
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isEmpty()) {
            throw new ResourceNotFoundException("Order not found");
        }

        Optional<User> user = userRepository.findById(vendorId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Transaction transaction = new Transaction();
        transaction.setOrder(order.get());
        transaction.setUser(user.get());
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.DISBURSEMENT);
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }

    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public List<Transaction> getTransactionsByOrder(Long orderId) {
        return transactionRepository.findByOrderId(orderId);
    }

    @Override
    public List<Transaction> getAllTransactions(
            Long userId,
            Long orderId,
            String startDate,
            String endDate,
            String transactionType,
            String sortBy
    ) {
        List<Transaction> transactions = transactionRepository.findAll();

        if (userId != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getUser().getId().equals(userId))
                    .collect(Collectors.toList());
        }

        if (orderId != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());
        }

        try{
        if (startDate != null && endDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            Date startDateNew = dateFormat.parse(startDate);
            Date endDateNew = dateFormat.parse(endDate);
            transactions = transactions.stream()
                    .filter(transaction ->
                            !transaction.getTransactionDate().before(startDateNew) &&
                                    !transaction.getTransactionDate().after(endDateNew))
                    .collect(Collectors.toList());
        }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (transactionType != null) {
            transactions = transactions.stream()
                    .filter(transaction -> transaction.getTransactionType().toString().equals(transactionType))
                    .collect(Collectors.toList());
        }

        switch (sortBy) {
            case "amountAsc":
                transactions.sort(Comparator.comparingDouble(Transaction::getAmount));
                break;
            case "amountDesc":
                transactions.sort(Comparator.comparingDouble(Transaction::getAmount).reversed());
                break;
            case "dateAsc":
                transactions.sort(Comparator.comparing(Transaction::getTransactionDate));
                break;
            case "dateDesc":
                transactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());
                break;
        }

        return transactions;
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
