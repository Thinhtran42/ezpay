package com.thinhtran.EzPay.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    private User sender;
    private User receiver;
    private LocalDateTime testDateTime;

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

        testDateTime = LocalDateTime.now();
    }

    @Test
    void transactionBuilder_Success() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(200.0)
                .message("Test transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(200.0, transaction.getAmount());
        assertEquals("Test transaction", transaction.getMessage());
        assertEquals(testDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionBuilder_WithNullValues() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message(null)
                .createdAt(testDateTime)
                .build();

        // Assert
        assertNotNull(transaction);
        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(100.0, transaction.getAmount());
        assertNull(transaction.getMessage());
        assertEquals(testDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionConstructor_NoArgs() {
        // Arrange & Act
        Transaction transaction = new Transaction();

        // Assert
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getSender());
        assertNull(transaction.getReceiver());
        assertNull(transaction.getAmount());
        assertNull(transaction.getMessage());
        assertNull(transaction.getCreatedAt());
    }

    @Test
    void transactionConstructor_AllArgs() {
        // Arrange & Act
        Transaction transaction = new Transaction(1L, sender, receiver, 250.0, "All args test", testDateTime);

        // Assert
        assertNotNull(transaction);
        assertEquals(1L, transaction.getId());
        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(250.0, transaction.getAmount());
        assertEquals("All args test", transaction.getMessage());
        assertEquals(testDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionSetters_Success() {
        // Arrange
        Transaction transaction = new Transaction();
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(1);

        // Act
        transaction.setId(2L);
        transaction.setSender(sender);
        transaction.setReceiver(receiver);
        transaction.setAmount(300.0);
        transaction.setMessage("Updated message");
        transaction.setCreatedAt(newDateTime);

        // Assert
        assertEquals(2L, transaction.getId());
        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(300.0, transaction.getAmount());
        assertEquals("Updated message", transaction.getMessage());
        assertEquals(newDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionGetters_Success() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(3L)
                .sender(sender)
                .receiver(receiver)
                .amount(150.0)
                .message("Getter test")
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertEquals(3L, transaction.getId());
        assertEquals(sender, transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
        assertEquals(150.0, transaction.getAmount());
        assertEquals("Getter test", transaction.getMessage());
        assertEquals(testDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionEquals_SameId() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .sender(receiver)
                .receiver(sender)
                .amount(200.0)
                .message("Transaction 2")
                .createdAt(testDateTime.plusMinutes(1))
                .build();

        // Act & Assert
        assertEquals(transaction1, transaction2); // Should be equal if same ID
    }

    @Test
    void transactionEquals_DifferentId() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(2L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertNotEquals(transaction1, transaction2); // Should not be equal if different ID
    }

    @Test
    void transactionEquals_NullId() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        Transaction transaction2 = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        // Act & Assert
        assertNotEquals(transaction1, transaction2); // Should not be equal if both have null ID
    }

    @Test
    void transactionHashCode_Consistency() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Test transaction")
                .createdAt(testDateTime)
                .build();

        // Act
        int hashCode1 = transaction.hashCode();
        int hashCode2 = transaction.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void transactionHashCode_EqualObjects() {
        // Arrange
        Transaction transaction1 = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Transaction 1")
                .createdAt(testDateTime)
                .build();

        Transaction transaction2 = Transaction.builder()
                .id(1L)
                .sender(receiver)
                .receiver(sender)
                .amount(200.0)
                .message("Transaction 2")
                .createdAt(testDateTime.plusMinutes(1))
                .build();

        // Act & Assert
        assertEquals(transaction1.hashCode(), transaction2.hashCode());
    }

    @Test
    void transactionToString_NotNull() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .id(1L)
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Test transaction")
                .createdAt(testDateTime)
                .build();

        // Act
        String toString = transaction.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("Transaction"));
        assertTrue(toString.contains("100.0"));
        assertTrue(toString.contains("Test transaction"));
    }

    @Test
    void transactionAmount_ZeroValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(0.0)
                .message("Zero amount transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(0.0, transaction.getAmount());
    }

    @Test
    void transactionAmount_PositiveValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(999.99)
                .message("Positive amount transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(999.99, transaction.getAmount());
    }

    @Test
    void transactionAmount_NegativeValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(-50.0)
                .message("Negative amount transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(-50.0, transaction.getAmount());
    }

    @Test
    void transactionAmount_LargeValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(999999999.99)
                .message("Large amount transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(999999999.99, transaction.getAmount());
    }

    @Test
    void transactionAmount_SmallDecimalValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(0.01)
                .message("Small decimal amount transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(0.01, transaction.getAmount());
    }

    @Test
    void transactionMessage_NullValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message(null)
                .createdAt(testDateTime)
                .build();

        // Assert
        assertNull(transaction.getMessage());
    }

    @Test
    void transactionMessage_EmptyString() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals("", transaction.getMessage());
    }

    @Test
    void transactionMessage_SpecialCharacters() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Payment for order #12345 - $100.00")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals("Payment for order #12345 - $100.00", transaction.getMessage());
    }

    @Test
    void transactionMessage_UnicodeCharacters() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("转账给朋友")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals("转账给朋友", transaction.getMessage());
    }

    @Test
    void transactionMessage_LongMessage() {
        // Arrange
        String longMessage = "This is a very long message that contains many characters to test the ability of the system to handle long messages in transaction descriptions. " +
                "It should work fine as the message field is likely configured to handle text of reasonable length. " +
                "This message is intended to test edge cases and ensure proper handling of longer text content.";

        // Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message(longMessage)
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(longMessage, transaction.getMessage());
    }

    @Test
    void transactionSelfTransfer_SameUser() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(sender)
                .amount(50.0)
                .message("Self transfer")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(sender, transaction.getSender());
        assertEquals(sender, transaction.getReceiver());
        assertEquals(50.0, transaction.getAmount());
        assertEquals("Self transfer", transaction.getMessage());
    }

    @Test
    void transactionCreatedAt_PastDateTime() {
        // Arrange
        LocalDateTime pastDateTime = LocalDateTime.now().minusDays(1);

        // Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Past transaction")
                .createdAt(pastDateTime)
                .build();

        // Assert
        assertEquals(pastDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionCreatedAt_FutureDateTime() {
        // Arrange
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);

        // Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Future transaction")
                .createdAt(futureDateTime)
                .build();

        // Assert
        assertEquals(futureDateTime, transaction.getCreatedAt());
    }

    @Test
    void transactionCreatedAt_NullValue() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Null date transaction")
                .createdAt(null)
                .build();

        // Assert
        assertNull(transaction.getCreatedAt());
    }

    @Test
    void transactionUsers_DifferentRoles() {
        // Arrange
        User adminUser = User.builder()
                .id(3L)
                .userName("admin")
                .email("admin@example.com")
                .password("password")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .balance(5000.0)
                .build();

        // Act
        Transaction transaction = Transaction.builder()
                .sender(adminUser)
                .receiver(sender)
                .amount(500.0)
                .message("Admin to user transfer")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(adminUser, transaction.getSender());
        assertEquals(sender, transaction.getReceiver());
        assertEquals(Role.ADMIN, transaction.getSender().getRole());
        assertEquals(Role.USER, transaction.getReceiver().getRole());
    }

    @Test
    void transactionUsers_NullSender() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(null)
                .receiver(receiver)
                .amount(100.0)
                .message("Null sender transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertNull(transaction.getSender());
        assertEquals(receiver, transaction.getReceiver());
    }

    @Test
    void transactionUsers_NullReceiver() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(null)
                .amount(100.0)
                .message("Null receiver transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(sender, transaction.getSender());
        assertNull(transaction.getReceiver());
    }

    @Test
    void transactionUsers_BothNull() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(null)
                .receiver(null)
                .amount(100.0)
                .message("Both null transaction")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertNull(transaction.getSender());
        assertNull(transaction.getReceiver());
    }

    @Test
    void transactionRelationships_UserReferences() {
        // Arrange & Act
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Relationship test")
                .createdAt(testDateTime)
                .build();

        // Assert
        assertEquals(sender.getId(), transaction.getSender().getId());
        assertEquals(receiver.getId(), transaction.getReceiver().getId());
        assertEquals(sender.getUserName(), transaction.getSender().getUserName());
        assertEquals(receiver.getUserName(), transaction.getReceiver().getUserName());
    }

    @Test
    void transactionImmutability_ModifyingUsers() {
        // Arrange
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .receiver(receiver)
                .amount(100.0)
                .message("Immutability test")
                .createdAt(testDateTime)
                .build();

        // Act - Modify original user objects
        String originalSenderName = sender.getUserName();
        String originalReceiverName = receiver.getUserName();
        sender.setUserName("modified_sender");
        receiver.setUserName("modified_receiver");

        // Assert - Transaction should still reference the same objects
        assertEquals("modified_sender", transaction.getSender().getUserName());
        assertEquals("modified_receiver", transaction.getReceiver().getUserName());
        
        // Restore original values
        sender.setUserName(originalSenderName);
        receiver.setUserName(originalReceiverName);
    }
} 