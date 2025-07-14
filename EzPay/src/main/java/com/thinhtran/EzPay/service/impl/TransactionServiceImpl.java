package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.TopUpRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.StatisticsResponse;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.exception.InsufficientBalanceException;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.exception.ValidationException;
import com.thinhtran.EzPay.entity.NotificationType;
import com.thinhtran.EzPay.repository.TransactionRepository;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.service.NotificationService;
import com.thinhtran.EzPay.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void transfer(String senderUsername, TransferRequest request) {
        // Business validation
        if (request.getAmount() == null) {
            throw new ValidationException("Amount cannot be null");
        }
        if (request.getAmount() <= 0) {
            throw new ValidationException("Amount must be positive");
        }
        
        var sender = userRepository.findByUserName(senderUsername)
                .orElseThrow(() -> new UserNotFoundException(senderUsername));
        var receiver = userRepository.findByUserName(request.getReceiverUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getReceiverUsername()));

        // Validate sender cannot transfer to themselves
        if (sender.getId().equals(receiver.getId())) {
            throw new ValidationException("Cannot transfer to yourself");
        }

        // Check balance
        if (sender.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException(sender.getBalance(), request.getAmount());
        }

        // Perform transfer
        sender.setBalance(sender.getBalance() - request.getAmount());
        receiver.setBalance(receiver.getBalance() + request.getAmount());

        var transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        userRepository.save(sender);
        userRepository.save(receiver);

        // Send notifications
        String senderMessage = String.format("Bạn đã chuyển %.0f VND cho %s", 
                request.getAmount(), receiver.getFullName());
        String receiverMessage = String.format("Bạn đã nhận %.0f VND từ %s", 
                request.getAmount(), sender.getFullName());

        notificationService.createAndSendNotification(
                sender.getId(),
                NotificationType.TRANSFER_SENT,
                "Chuyển tiền thành công",
                senderMessage,
                savedTransaction.getId()
        );

        notificationService.createAndSendNotification(
                receiver.getId(),
                NotificationType.TRANSFER_RECEIVED,
                "Nhận tiền",
                receiverMessage,
                savedTransaction.getId()
        );
    }

    @Override
    public List<TransactionResponse> getHistory(String username) {
        var user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        var list = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(user, user);

        return list.stream().map(tx -> {
            var res = new TransactionResponse();
            res.setSenderUsername(tx.getSender().getUserName());
            res.setReceiverUsername(tx.getReceiver().getUserName());
            res.setAmount(tx.getAmount());
            res.setMessage(tx.getMessage());
            res.setCreatedAt(tx.getCreatedAt());
            return res;
        }).toList();
    }

    @Override
    @Transactional
    public void topUp(TopUpRequest request) {
        // Business validation
        if (request.getAmount() == null) {
            throw new ValidationException("Amount cannot be null");
        }
        if (request.getAmount() <= 0) {
            throw new ValidationException("Amount must be positive");
        }

        var targetUser = userRepository.findByUserName(request.getTargetUsername())
                .orElseThrow(() -> new UserNotFoundException(request.getTargetUsername()));

        // Additional validation: check for reasonable top-up limits
        double maxTopUpAmount = 10_000_000.0; // 10 million
        if (request.getAmount() > maxTopUpAmount) {
            throw new ValidationException("Top-up amount exceeds maximum limit of " + maxTopUpAmount);
        }

        // Check if resulting balance would exceed maximum allowed
        double maxBalance = 999_999_999.99;
        if (targetUser.getBalance() + request.getAmount() > maxBalance) {
            throw new ValidationException("Top-up would exceed maximum account balance limit");
        }

        targetUser.setBalance(targetUser.getBalance() + request.getAmount());
        userRepository.save(targetUser);
    }

    @Override
    public StatisticsResponse getStatistics() {
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // Calculate total transferred amount
        Double totalTransferred = allTransactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        // Calculate total number of transactions
        Integer totalTransactions = allTransactions.size();

        // Calculate top receivers
        var topReceiversMap = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        tx -> tx.getReceiver().getUserName(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                transactions -> {
                                    var receiver = transactions.get(0).getReceiver();
                                    var totalReceived = transactions.stream()
                                            .mapToDouble(Transaction::getAmount)
                                            .sum();
                                    return new StatisticsResponse.TopReceiverResponse(
                                            receiver.getUserName(),
                                            receiver.getFullName(),
                                            totalReceived,
                                            transactions.size()
                                    );
                                }
                        )
                ));

        List<StatisticsResponse.TopReceiverResponse> topReceivers = topReceiversMap.values()
                .stream()
                .sorted((a, b) -> Double.compare(b.getTotalReceived(), a.getTotalReceived()))
                .limit(10)
                .collect(Collectors.toList());

        return new StatisticsResponse(totalTransferred, totalTransactions, topReceivers);
    }
}