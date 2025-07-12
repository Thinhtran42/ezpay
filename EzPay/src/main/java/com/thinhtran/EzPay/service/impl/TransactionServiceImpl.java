package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.repository.TransactionRepository;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void transfer(String senderUsername, TransferRequest request) {
        // Validate amount
        if (request.getAmount() == null) {
            throw new RuntimeException("Amount cannot be null");
        }
        if (request.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        var sender = userRepository.findByUserName(senderUsername).orElseThrow();
        var receiver = userRepository.findByUserName(request.getReceiverUsername()).orElseThrow();

        if (sender.getBalance() < request.getAmount()) {
            throw new RuntimeException("Số dư không đủ");
        }

        sender.setBalance(sender.getBalance() - request.getAmount());
        receiver.setBalance(receiver.getBalance() + request.getAmount());

        var transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(request.getAmount())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    @Override
    public List<TransactionResponse> getHistory(String username) {
        var user = userRepository.findByUserName(username).orElseThrow();
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
}