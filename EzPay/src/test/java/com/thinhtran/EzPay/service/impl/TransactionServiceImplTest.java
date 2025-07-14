package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.TopUpRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.StatisticsResponse;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.InsufficientBalanceException;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.exception.ValidationException;
import com.thinhtran.EzPay.repository.TransactionRepository;
import com.thinhtran.EzPay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User sender;
    private User receiver;
    private User targetUser;
    private TransferRequest transferRequest;
    private TopUpRequest topUpRequest;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .userName("sender")
                .email("sender@example.com")
                .password("password")
                .fullName("Sender User")
                .role(Role.USER)
                .balance(1000.0)
                .build();

        receiver = User.builder()
                .id(2L)
                .userName("receiver")
                .email("receiver@example.com")
                .password("password")
                .fullName("Receiver User")
                .role(Role.USER)
                .balance(500.0)
                .build();

        targetUser = User.builder()
                .id(3L)
                .userName("target")
                .email("target@example.com")
                .password("password")
                .fullName("Target User")
                .role(Role.USER)
                .balance(100.0)
                .build();

        transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(200.0);
        transferRequest.setMessage("Test transfer");

        topUpRequest = new TopUpRequest();
        topUpRequest.setTargetUsername("target");
        topUpRequest.setAmount(500.0);

        testTransaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ======= TRANSFER TESTS =======
    @Test
    void transfer_Success() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        transactionService.transfer("sender", transferRequest);

        // Assert
        assertEquals(800.0, sender.getBalance()); // 1000 - 200
        assertEquals(700.0, receiver.getBalance()); // 500 + 200
        
        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verify(transactionRepository).save(any(Transaction.class));
        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);
    }

    @Test
    void transfer_InsufficientBalance() {
        // Arrange
        transferRequest.setAmount(1500.0); // More than sender's balance of 1000
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Số dư không đủ. Số dư hiện tại: 1000,00, Số tiền cần: 1500,00", exception.getMessage());
        assertEquals(1000.0, sender.getBalance()); // Balance unchanged
        assertEquals(500.0, receiver.getBalance()); // Balance unchanged
        
        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_SenderNotFound() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("User not found: sender", exception.getMessage());
        verify(userRepository).findByUserName("sender");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ReceiverNotFound() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("User not found: receiver", exception.getMessage());
        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ZeroAmount() {
        // Arrange
        transferRequest.setAmount(0.0);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_NegativeAmount() {
        // Arrange
        transferRequest.setAmount(-100.0);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_NullAmount() {
        // Arrange
        transferRequest.setAmount(null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Amount cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_SelfTransfer() {
        // Arrange
        transferRequest.setReceiverUsername("sender"); // Same as sender
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Cannot transfer to yourself", exception.getMessage());
        verify(userRepository, times(2)).findByUserName("sender"); // Called twice - sender and receiver
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ExactBalance() {
        // Arrange
        transferRequest.setAmount(1000.0); // Exact balance
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        transactionService.transfer("sender", transferRequest);

        // Assert
        assertEquals(0.0, sender.getBalance()); // 1000 - 1000
        assertEquals(1500.0, receiver.getBalance()); // 500 + 1000
        
        verify(transactionRepository).save(any(Transaction.class));
        verify(userRepository).save(sender);
        verify(userRepository).save(receiver);
    }

    @Test
    void transfer_SavesTransactionWithCorrectData() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act
        transactionService.transfer("sender", transferRequest);

        // Assert
        verify(transactionRepository).save(argThat(transaction -> 
            transaction.getSender().equals(sender) &&
            transaction.getReceiver().equals(receiver) &&
            transaction.getAmount().equals(200.0) &&
            transaction.getMessage().equals("Test transfer") &&
            transaction.getCreatedAt() != null
        ));
    }

    // ======= GET HISTORY TESTS =======
    @Test
    void getHistory_Success() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message("First transfer")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .sender(receiver)
                .receiver(sender)
                .amount(100.0)
                .message("Second transfer")
                .createdAt(LocalDateTime.now())
                .build();

        List<Transaction> transactions = Arrays.asList(transaction1, transaction2);
        
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(transactions);

        // Act
        List<TransactionResponse> result = transactionService.getHistory("sender");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        TransactionResponse firstResponse = result.get(0);
        assertEquals("sender", firstResponse.getSenderUsername());
        assertEquals("receiver", firstResponse.getReceiverUsername());
        assertEquals(200.0, firstResponse.getAmount());
        assertEquals("First transfer", firstResponse.getMessage());
        assertNotNull(firstResponse.getCreatedAt());
        
        TransactionResponse secondResponse = result.get(1);
        assertEquals("receiver", secondResponse.getSenderUsername());
        assertEquals("sender", secondResponse.getReceiverUsername());
        assertEquals(100.0, secondResponse.getAmount());
        assertEquals("Second transfer", secondResponse.getMessage());
        assertNotNull(secondResponse.getCreatedAt());
        
        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void getHistory_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            transactionService.getHistory("nonexistent");
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUserName("nonexistent");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getHistory_EmptyHistory() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(Collections.emptyList());

        // Act
        List<TransactionResponse> result = transactionService.getHistory("sender");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        
        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void getHistory_HandlesNullMessage() {
        // Arrange
        Transaction transactionWithNullMessage = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message(null)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(Arrays.asList(transactionWithNullMessage));

        // Act
        List<TransactionResponse> result = transactionService.getHistory("sender");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getMessage());
    }

    // ======= TOP UP TESTS =======
    @Test
    void topUp_Success() {
        // Arrange
        when(userRepository.findByUserName("target")).thenReturn(Optional.of(targetUser));

        // Act
        transactionService.topUp(topUpRequest);

        // Assert
        assertEquals(600.0, targetUser.getBalance()); // 100 + 500
        verify(userRepository).findByUserName("target");
        verify(userRepository).save(targetUser);
    }

    @Test
    void topUp_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName("nonexistent")).thenReturn(Optional.empty());
        topUpRequest.setTargetUsername("nonexistent");

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUserName("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void topUp_NullAmount() {
        // Arrange
        topUpRequest.setAmount(null);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("Amount cannot be null", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void topUp_ZeroAmount() {
        // Arrange
        topUpRequest.setAmount(0.0);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void topUp_NegativeAmount() {
        // Arrange
        topUpRequest.setAmount(-100.0);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        verifyNoInteractions(userRepository);
    }

    @Test
    void topUp_ExceedsMaximumTopUpAmount() {
        // Arrange
        topUpRequest.setAmount(15_000_000.0); // Exceeds 10 million limit
        when(userRepository.findByUserName("target")).thenReturn(Optional.of(targetUser));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("Top-up amount exceeds maximum limit of 1.0E7", exception.getMessage());
        verify(userRepository).findByUserName("target");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void topUp_ExceedsMaximumBalance() {
        // Arrange
        targetUser.setBalance(999_999_999.0); // Very high existing balance
        topUpRequest.setAmount(1000.0); // This would exceed max balance
        when(userRepository.findByUserName("target")).thenReturn(Optional.of(targetUser));

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            transactionService.topUp(topUpRequest);
        });

        assertEquals("Top-up would exceed maximum account balance limit", exception.getMessage());
        verify(userRepository).findByUserName("target");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void topUp_MaximumAllowedAmount() {
        // Arrange
        topUpRequest.setAmount(10_000_000.0); // Maximum allowed
        when(userRepository.findByUserName("target")).thenReturn(Optional.of(targetUser));

        // Act
        transactionService.topUp(topUpRequest);

        // Assert
        assertEquals(10_000_100.0, targetUser.getBalance()); // 100 + 10,000,000
        verify(userRepository).findByUserName("target");
        verify(userRepository).save(targetUser);
    }

    // ======= STATISTICS TESTS =======
    @Test
    void getStatistics_Success() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .build();

        Transaction transaction2 = Transaction.builder()
                .sender(receiver)
                .receiver(targetUser)
                .amount(300.0)
                .build();

        Transaction transaction3 = Transaction.builder()
                .sender(sender)
                .receiver(targetUser)
                .amount(150.0)
                .build();

        List<Transaction> allTransactions = Arrays.asList(transaction1, transaction2, transaction3);
        when(transactionRepository.findAll()).thenReturn(allTransactions);

        // Act
        StatisticsResponse result = transactionService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(650.0, result.getTotalTransferred()); // 200 + 300 + 150
        assertEquals(3, result.getTotalTransactions());
        
        assertNotNull(result.getTopReceivers());
        assertEquals(2, result.getTopReceivers().size());
        
        // Check top receiver (target should be first with 450.0 total)
        StatisticsResponse.TopReceiverResponse topReceiver = result.getTopReceivers().get(0);
        assertEquals("target", topReceiver.getUsername());
        assertEquals("Target User", topReceiver.getFullName());
        assertEquals(450.0, topReceiver.getTotalReceived()); // 300 + 150
        assertEquals(2, topReceiver.getTransactionCount());
        
        // Check second receiver
        StatisticsResponse.TopReceiverResponse secondReceiver = result.getTopReceivers().get(1);
        assertEquals("receiver", secondReceiver.getUsername());
        assertEquals("Receiver User", secondReceiver.getFullName());
        assertEquals(200.0, secondReceiver.getTotalReceived());
        assertEquals(1, secondReceiver.getTransactionCount());

        verify(transactionRepository).findAll();
    }

    @Test
    void getStatistics_EmptyTransactions() {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        StatisticsResponse result = transactionService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0.0, result.getTotalTransferred());
        assertEquals(0, result.getTotalTransactions());
        assertNotNull(result.getTopReceivers());
        assertEquals(0, result.getTopReceivers().size());

        verify(transactionRepository).findAll();
    }

    @Test
    void getStatistics_SingleTransaction() {
        // Arrange
        Transaction singleTransaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(500.0)
                .build();

        when(transactionRepository.findAll()).thenReturn(Arrays.asList(singleTransaction));

        // Act
        StatisticsResponse result = transactionService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(500.0, result.getTotalTransferred());
        assertEquals(1, result.getTotalTransactions());
        
        assertNotNull(result.getTopReceivers());
        assertEquals(1, result.getTopReceivers().size());
        
        StatisticsResponse.TopReceiverResponse topReceiver = result.getTopReceivers().get(0);
        assertEquals("receiver", topReceiver.getUsername());
        assertEquals("Receiver User", topReceiver.getFullName());
        assertEquals(500.0, topReceiver.getTotalReceived());
        assertEquals(1, topReceiver.getTransactionCount());

        verify(transactionRepository).findAll();
    }

    @Test
    void getStatistics_LimitsTopReceiversToTen() {
        // Arrange - Create 15 different receivers
        List<Transaction> transactions = Arrays.asList();
        List<Transaction> mutableTransactions = new java.util.ArrayList<>(transactions);
        
        for (int i = 1; i <= 15; i++) {
            User receiver = User.builder()
                    .id((long) (i + 10))
                    .userName("receiver" + i)
                    .fullName("Receiver " + i)
                    .build();
                    
            Transaction transaction = Transaction.builder()
                    .sender(sender)
                    .receiver(receiver)
                    .amount((double) i * 100) // Different amounts for sorting
                    .build();
                    
            mutableTransactions.add(transaction);
        }
        
        when(transactionRepository.findAll()).thenReturn(mutableTransactions);

        // Act
        StatisticsResponse result = transactionService.getStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(15, result.getTotalTransactions());
        assertNotNull(result.getTopReceivers());
        assertEquals(10, result.getTopReceivers().size()); // Limited to 10
        
        // Check that they are sorted by total received (descending)
        StatisticsResponse.TopReceiverResponse firstReceiver = result.getTopReceivers().get(0);
        StatisticsResponse.TopReceiverResponse lastReceiver = result.getTopReceivers().get(9);
        assertTrue(firstReceiver.getTotalReceived() >= lastReceiver.getTotalReceived());

        verify(transactionRepository).findAll();
    }
} 