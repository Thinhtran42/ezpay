package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.entity.User;
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
    private TransferRequest transferRequest;
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

        transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(200.0);
        transferRequest.setMessage("Test transfer");

        testTransaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();
    }

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
    }

    @Test
    void transfer_InsufficientBalance() {
        // Arrange
        transferRequest.setAmount(1500.0); // More than sender's balance of 1000
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Số dư không đủ", exception.getMessage());
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
        assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

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
        assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ZeroAmount() {
        // Arrange
        transferRequest.setAmount(0.0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });
        
        assertEquals("Amount must be positive", exception.getMessage());
        assertEquals(1000.0, sender.getBalance()); // Balance unchanged
        assertEquals(500.0, receiver.getBalance()); // Balance unchanged
        
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_NegativeAmount() {
        // Arrange
        transferRequest.setAmount(-100.0);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.transfer("sender", transferRequest);
        });

        assertEquals("Amount must be positive", exception.getMessage());
        assertEquals(1000.0, sender.getBalance()); // Balance unchanged
        assertEquals(500.0, receiver.getBalance()); // Balance unchanged
        
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
        assertThrows(RuntimeException.class, () -> {
            transactionService.getHistory("nonexistent");
        });

        verify(userRepository).findByUserName("nonexistent");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getHistory_EmptyHistory() {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(Arrays.asList());

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

    @Test
    void transfer_SelfTransfer() {
        // Arrange
        transferRequest.setReceiverUsername("sender"); // Same as sender
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act
        transactionService.transfer("sender", transferRequest);

        // Assert
        assertEquals(1000.0, sender.getBalance()); // No net change in balance
        verify(transactionRepository).save(any(Transaction.class));
    }
} 