package com.thinhtran.EzPay.repository;

import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TransactionRepository transactionRepository;

    private User sender;
    private User receiver;
    private User thirdUser;
    private Transaction transaction1;
    private Transaction transaction2;
    private Transaction transaction3;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .userName("sender")
                .email("sender@example.com")
                .password("password")
                .fullName("Sender User")
                .role(Role.USER)
                .balance(1000.0)
                .build();

        receiver = User.builder()
                .userName("receiver")
                .email("receiver@example.com")
                .password("password")
                .fullName("Receiver User")
                .role(Role.USER)
                .balance(500.0)
                .build();

        thirdUser = User.builder()
                .userName("third")
                .email("third@example.com")
                .password("password")
                .fullName("Third User")
                .role(Role.USER)
                .balance(300.0)
                .build();

        // Persist users first
        sender = entityManager.persistAndFlush(sender);
        receiver = entityManager.persistAndFlush(receiver);
        thirdUser = entityManager.persistAndFlush(thirdUser);

        transaction1 = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message("First transaction")
                .createdAt(LocalDateTime.now().minusDays(3))
                .build();

        transaction2 = Transaction.builder()
                .sender(receiver)
                .receiver(sender)
                .amount(100.0)
                .message("Second transaction")
                .createdAt(LocalDateTime.now().minusDays(2))
                .build();

        transaction3 = Transaction.builder()
                .sender(thirdUser)
                .receiver(receiver)
                .amount(50.0)
                .message("Third transaction")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_SenderTransactions() {
        // Arrange
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);
        entityManager.persistAndFlush(transaction3);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);

        // Assert
        assertEquals(2, result.size());
        
        // Check that transactions are ordered by createdAt desc
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
        
        // Check that both transactions involve the sender
        assertTrue(result.stream().allMatch(t -> 
            t.getSender().equals(sender) || t.getReceiver().equals(sender)));
        
        // Verify specific transactions
        assertTrue(result.stream().anyMatch(t -> 
            t.getSender().equals(sender) && t.getReceiver().equals(receiver) && t.getAmount().equals(200.0)));
        assertTrue(result.stream().anyMatch(t -> 
            t.getSender().equals(receiver) && t.getReceiver().equals(sender) && t.getAmount().equals(100.0)));
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_ReceiverTransactions() {
        // Arrange
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);
        entityManager.persistAndFlush(transaction3);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(receiver, receiver);

        // Assert
        assertEquals(3, result.size());
        
        // Check that transactions are ordered by createdAt desc
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getCreatedAt().isAfter(result.get(i + 1).getCreatedAt()) ||
                      result.get(i).getCreatedAt().equals(result.get(i + 1).getCreatedAt()));
        }
        
        // Check that all transactions involve the receiver
        assertTrue(result.stream().allMatch(t -> 
            t.getSender().equals(receiver) || t.getReceiver().equals(receiver)));
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_NoTransactions() {
        // Arrange
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);
        entityManager.persistAndFlush(transaction3);

        User userWithNoTransactions = User.builder()
                .userName("notransactions")
                .email("notransactions@example.com")
                .password("password")
                .fullName("No Transactions User")
                .role(Role.USER)
                .balance(0.0)
                .build();
        userWithNoTransactions = entityManager.persistAndFlush(userWithNoTransactions);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(
            userWithNoTransactions, userWithNoTransactions);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_SameDateTime() {
        // Arrange
        LocalDateTime sameTime = LocalDateTime.now();
        
        Transaction tx1 = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Same time tx1")
                .createdAt(sameTime)
                .build();

        Transaction tx2 = Transaction.builder()
                .sender(receiver)
                .receiver(sender)
                .amount(50.0)
                .message("Same time tx2")
                .createdAt(sameTime)
                .build();

        entityManager.persistAndFlush(tx1);
        entityManager.persistAndFlush(tx2);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);

        // Assert
        assertEquals(2, result.size());
        assertEquals(sameTime, result.get(0).getCreatedAt());
        assertEquals(sameTime, result.get(1).getCreatedAt());
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_NullMessage() {
        // Arrange
        Transaction txWithNullMessage = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(150.0)
                .message(null)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(txWithNullMessage);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.get(0).getMessage());
        assertEquals(150.0, result.get(0).getAmount());
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_ZeroAmount() {
        // Arrange
        Transaction txWithZeroAmount = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(0.0)
                .message("Zero amount transaction")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(txWithZeroAmount);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0.0, result.get(0).getAmount());
        assertEquals("Zero amount transaction", result.get(0).getMessage());
    }

    @Test
    void save_Success() {
        // Act
        Transaction savedTransaction = transactionRepository.save(transaction1);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(sender, savedTransaction.getSender());
        assertEquals(receiver, savedTransaction.getReceiver());
        assertEquals(200.0, savedTransaction.getAmount());
        assertEquals("First transaction", savedTransaction.getMessage());
        assertNotNull(savedTransaction.getCreatedAt());
    }

    @Test
    void save_WithNullMessage() {
        // Arrange
        transaction1.setMessage(null);

        // Act
        Transaction savedTransaction = transactionRepository.save(transaction1);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertNull(savedTransaction.getMessage());
        assertEquals(200.0, savedTransaction.getAmount());
    }

    @Test
    void save_WithEmptyMessage() {
        // Arrange
        transaction1.setMessage("");

        // Act
        Transaction savedTransaction = transactionRepository.save(transaction1);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals("", savedTransaction.getMessage());
        assertEquals(200.0, savedTransaction.getAmount());
    }

    @Test
    void findById_Success() {
        // Arrange
        Transaction savedTransaction = entityManager.persistAndFlush(transaction1);

        // Act
        Optional<Transaction> result = transactionRepository.findById(savedTransaction.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedTransaction.getId(), result.get().getId());
        assertEquals(sender, result.get().getSender());
        assertEquals(receiver, result.get().getReceiver());
        assertEquals(200.0, result.get().getAmount());
    }

    @Test
    void findById_NotFound() {
        // Act
        Optional<Transaction> result = transactionRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_Success() {
        // Arrange
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);
        entityManager.persistAndFlush(transaction3);

        // Act
        List<Transaction> result = transactionRepository.findAll();

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getAmount().equals(200.0)));
        assertTrue(result.stream().anyMatch(t -> t.getAmount().equals(100.0)));
        assertTrue(result.stream().anyMatch(t -> t.getAmount().equals(50.0)));
    }

    @Test
    void findAll_Empty() {
        // Act
        List<Transaction> result = transactionRepository.findAll();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteById_Success() {
        // Arrange
        Transaction savedTransaction = entityManager.persistAndFlush(transaction1);
        Long transactionId = savedTransaction.getId();

        // Act
        transactionRepository.deleteById(transactionId);

        // Assert
        Optional<Transaction> result = transactionRepository.findById(transactionId);
        assertFalse(result.isPresent());
    }

    @Test
    void count_Success() {
        // Arrange
        entityManager.persistAndFlush(transaction1);
        entityManager.persistAndFlush(transaction2);
        entityManager.persistAndFlush(transaction3);

        // Act
        long count = transactionRepository.count();

        // Assert
        assertEquals(3, count);
    }

    @Test
    void count_Empty() {
        // Act
        long count = transactionRepository.count();

        // Assert
        assertEquals(0, count);
    }

    @Test
    void relationships_SenderReceiver() {
        // Arrange
        Transaction savedTransaction = entityManager.persistAndFlush(transaction1);

        // Act
        Optional<Transaction> result = transactionRepository.findById(savedTransaction.getId());

        // Assert
        assertTrue(result.isPresent());
        Transaction tx = result.get();
        
        // Check that relationships are properly maintained
        assertEquals(sender.getId(), tx.getSender().getId());
        assertEquals(receiver.getId(), tx.getReceiver().getId());
        assertEquals(sender.getUserName(), tx.getSender().getUserName());
        assertEquals(receiver.getUserName(), tx.getReceiver().getUserName());
    }

    @Test
    void selfTransfer_Transaction() {
        // Arrange
        Transaction selfTransfer = Transaction.builder()
                .sender(sender)
                .receiver(sender) // Same user
                .amount(50.0)
                .message("Self transfer")
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Transaction savedTransaction = transactionRepository.save(selfTransfer);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(sender, savedTransaction.getSender());
        assertEquals(sender, savedTransaction.getReceiver());
        assertEquals(50.0, savedTransaction.getAmount());
        assertEquals("Self transfer", savedTransaction.getMessage());
    }

    @Test
    void findBySenderOrReceiverOrderByCreatedAtDesc_SelfTransfer() {
        // Arrange
        Transaction selfTransfer = Transaction.builder()
                .sender(sender)
                .receiver(sender)
                .amount(50.0)
                .message("Self transfer")
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persistAndFlush(selfTransfer);

        // Act
        List<Transaction> result = transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);

        // Assert
        assertEquals(1, result.size());
        assertEquals(sender, result.get(0).getSender());
        assertEquals(sender, result.get(0).getReceiver());
        assertEquals(50.0, result.get(0).getAmount());
    }

    @Test
    void largeAmount_Transaction() {
        // Arrange
        Transaction largeAmountTx = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(999999.99)
                .message("Large amount")
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Transaction savedTransaction = transactionRepository.save(largeAmountTx);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(999999.99, savedTransaction.getAmount());
        assertEquals("Large amount", savedTransaction.getMessage());
    }

    @Test
    void verySmallAmount_Transaction() {
        // Arrange
        Transaction smallAmountTx = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(0.01)
                .message("Small amount")
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        Transaction savedTransaction = transactionRepository.save(smallAmountTx);

        // Assert
        assertNotNull(savedTransaction.getId());
        assertEquals(0.01, savedTransaction.getAmount());
        assertEquals("Small amount", savedTransaction.getMessage());
    }
} 