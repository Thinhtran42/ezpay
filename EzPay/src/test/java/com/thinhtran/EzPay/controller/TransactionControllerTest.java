package com.thinhtran.EzPay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.TransactionResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.Transaction;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.repository.TransactionRepository;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTThatIsAtLeast256BitsLong!",
    "jwt.expirationMs=86400000"
})
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User sender;
    private User receiver;
    private TransferRequest transferRequest;
    private String validToken;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        sender = User.builder()
                .id(1L)
                .userName("sender")
                .email("sender@example.com")
                .password("encodedPassword")
                .fullName("Sender User")
                .role(Role.USER)
                .balance(1000.0)
                .build();

        receiver = User.builder()
                .id(2L)
                .userName("receiver")
                .email("receiver@example.com")
                .password("encodedPassword")
                .fullName("Receiver User")
                .role(Role.USER)
                .balance(500.0)
                .build();

        transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(200.0);
        transferRequest.setMessage("Test transfer");

        validToken = jwtTokenProvider.generateToken("sender");

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
    void transfer_Success() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_WithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_InsufficientBalance() throws Exception {
        // Arrange
        transferRequest.setAmount(1500.0); // More than sender's balance
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ReceiverNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).findByUserName("sender");
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_SenderNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByUserName("sender");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ZeroAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(0.0);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_NegativeAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(-100.0);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_NullAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(null);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_EmptyReceiverUsername() throws Exception {
        // Arrange
        transferRequest.setReceiverUsername("");

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_SelfTransfer() throws Exception {
        // Arrange
        transferRequest.setReceiverUsername("sender"); // Same as sender
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_WithNullMessage() throws Exception {
        // Arrange
        transferRequest.setMessage(null);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_WithEmptyMessage() throws Exception {
        // Arrange
        transferRequest.setMessage("");
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void getHistory_Success() throws Exception {
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

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].senderUsername").value("sender"))
                .andExpect(jsonPath("$[0].receiverUsername").value("receiver"))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[0].message").value("First transfer"))
                .andExpect(jsonPath("$[1].senderUsername").value("receiver"))
                .andExpect(jsonPath("$[1].receiverUsername").value("sender"))
                .andExpect(jsonPath("$[1].amount").value(100.0))
                .andExpect(jsonPath("$[1].message").value("Second transfer"));

        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void getHistory_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void getHistory_WithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void getHistory_EmptyHistory() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void getHistory_UserNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByUserName("sender");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getHistory_WithNullMessages() throws Exception {
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

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].message").isEmpty());

        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void transfer_MalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void transfer_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, transactionRepository);
    }

    @Test
    void getHistory_ContentTypeVerification() throws Exception {
        // Arrange
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender))
                .thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        verify(userRepository).findByUserName("sender");
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(sender, sender);
    }

    @Test
    void transfer_LargeAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(999.99);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transfer_VerySmallAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(0.01);
        when(userRepository.findByUserName("sender")).thenReturn(Optional.of(sender));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        verify(transactionRepository).save(any(Transaction.class));
    }
} 