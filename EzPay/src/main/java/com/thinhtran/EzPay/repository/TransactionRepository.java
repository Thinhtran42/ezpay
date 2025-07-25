package com.thinhtran.EzPay.repository;

import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver);
}