package com.thinhtran.EzPay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.request.TopUpRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.StatisticsResponse;
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
import java.util.Collections;
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

    private User regularUser;
    private User adminUser;
    private User receiver;
    private User targetUser;
    private TransferRequest transferRequest;
    private TopUpRequest topUpRequest;
    private String userToken;
    private String adminToken;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id(1L)
                .userName("user")
                .email("user@example.com")
                .password("encodedPassword")
                .fullName("Regular User")
                .role(Role.USER)
                .balance(1000.0)
                .build();

        adminUser = User.builder()
                .id(2L)
                .userName("admin")
                .email("admin@example.com")
                .password("encodedPassword")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .balance(5000.0)
                .build();

        receiver = User.builder()
                .id(3L)
                .userName("receiver")
                .email("receiver@example.com")
                .password("encodedPassword")
                .fullName("Receiver User")
                .role(Role.USER)
                .balance(500.0)
                .build();

        targetUser = User.builder()
                .id(4L)
                .userName("target")
                .email("target@example.com")
                .password("encodedPassword")
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

        userToken = jwtTokenProvider.generateToken("user");
        adminToken = jwtTokenProvider.generateToken("admin");

        testTransaction = Transaction.builder()
                .id(1L)
                .sender(regularUser)
                .receiver(receiver)
                .amount(200.0)
                .message("Test transfer")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ======= TRANSFER TESTS =======
    @Test
    void transfer_Success() throws Exception {
        // Arrange
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Chuyển tiền thành công"));

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
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

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_WithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_InsufficientBalance() throws Exception {
        // Arrange
        transferRequest.setAmount(1500.0); // More than user's balance
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_ReceiverNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isNotFound());

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verify(userRepository).findByUserName("receiver");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_SelfTransfer() throws Exception {
        // Arrange
        transferRequest.setReceiverUsername("user"); // Same as sender
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(3)).findByUserName("user"); // JWT filter + service (2 calls for self-transfer check)
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void transfer_WithNullMessage() throws Exception {
        // Arrange
        transferRequest.setMessage(null);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiver));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Chuyển tiền thành công"));

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verify(userRepository).findByUserName("receiver");
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ======= GET HISTORY TESTS =======
    @Test
    void getHistory_Success() throws Exception {
        // Arrange
        List<Transaction> transactions = Arrays.asList(
                Transaction.builder()
                        .sender(regularUser)
                        .receiver(receiver)
                        .amount(200.0)
                        .message("First transfer")
                        .createdAt(LocalDateTime.of(2025, 7, 13, 17, 26, 10))
                        .build(),
                Transaction.builder()
                        .sender(receiver)
                        .receiver(regularUser)
                        .amount(100.0)
                        .message("Second transfer")
                        .createdAt(LocalDateTime.of(2025, 7, 14, 17, 26, 10))
                        .build()
        );

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(regularUser, regularUser)).thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử giao dịch thành công"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].senderUsername").value("user"))
                .andExpect(jsonPath("$.data[0].receiverUsername").value("receiver"))
                .andExpect(jsonPath("$.data[0].amount").value(200.0))
                .andExpect(jsonPath("$.data[0].message").value("First transfer"))
                .andExpect(jsonPath("$.data[1].senderUsername").value("receiver"))
                .andExpect(jsonPath("$.data[1].receiverUsername").value("user"))
                .andExpect(jsonPath("$.data[1].amount").value(100.0))
                .andExpect(jsonPath("$.data[1].message").value("Second transfer"));

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(regularUser, regularUser);
    }

    @Test
    void getHistory_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getHistory_EmptyHistory() throws Exception {
        // Arrange
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(transactionRepository.findBySenderOrReceiverOrderByCreatedAtDesc(regularUser, regularUser)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy lịch sử giao dịch thành công"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verify(transactionRepository).findBySenderOrReceiverOrderByCreatedAtDesc(regularUser, regularUser);
    }

    // ======= TOP UP TESTS =======
    @Test
    void topUp_Success() throws Exception {
        // Arrange
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUserName("target")).thenReturn(Optional.of(targetUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Nạp tiền thành công"));

        verify(userRepository, atLeastOnce()).findByUserName("admin"); // JWT filter + service
        verify(userRepository, atLeastOnce()).findByUserName("target");
    }

    @Test
    void topUp_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void topUp_WithUserRole() throws Exception {
        // Arrange
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isForbidden());

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void topUp_TargetUserNotFound() throws Exception {
        // Arrange
        topUpRequest.setTargetUsername("nonexistent");
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUserName("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isNotFound());

        verify(userRepository, times(2)).findByUserName("admin"); // JWT filter + service
        verify(userRepository).findByUserName("nonexistent");
    }

    @Test
    void topUp_ExceedsMaximumAmount() throws Exception {
        // Arrange
        topUpRequest.setAmount(15_000_000.0); // Exceeds maximum
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(2)).findByUserName("admin"); // JWT filter + service
        verifyNoMoreInteractions(userRepository);
    }

    // ======= GET STATISTICS TESTS =======
    @Test
    void getStatistics_Success() throws Exception {
        // Arrange
        StatisticsResponse statisticsResponse = new StatisticsResponse();
        statisticsResponse.setTotalTransferred(1000.0);
        statisticsResponse.setTotalTransactions(5);
        statisticsResponse.setTopReceivers(Arrays.asList(
                new StatisticsResponse.TopReceiverResponse("receiver", "Receiver User", 600.0, 3),
                new StatisticsResponse.TopReceiverResponse("target", "Target User", 400.0, 2)
        ));

        List<Transaction> allTransactions = Arrays.asList(
                Transaction.builder().sender(regularUser).receiver(receiver).amount(300.0).build(),
                Transaction.builder().sender(adminUser).receiver(receiver).amount(300.0).build(),
                Transaction.builder().sender(regularUser).receiver(targetUser).amount(400.0).build()
        );

        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(transactionRepository.findAll()).thenReturn(allTransactions);

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thống kê thành công"))
                .andExpect(jsonPath("$.data.totalTransferred").value(1000.0))
                .andExpect(jsonPath("$.data.totalTransactions").value(3))
                .andExpect(jsonPath("$.data.topReceivers").isArray());

        verify(userRepository, times(2)).findByUserName("admin"); // JWT filter + service
        verify(transactionRepository).findAll();
    }

    @Test
    void getStatistics_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getStatistics_WithUserRole() throws Exception {
        // Arrange
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getStatistics_EmptyTransactions() throws Exception {
        // Arrange
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thống kê thành công"))
                .andExpect(jsonPath("$.data.totalTransferred").value(0.0))
                .andExpect(jsonPath("$.data.totalTransactions").value(0))
                .andExpect(jsonPath("$.data.topReceivers").isArray())
                .andExpect(jsonPath("$.data.topReceivers.length()").value(0));

        verify(userRepository, times(2)).findByUserName("admin"); // JWT filter + service
        verify(transactionRepository).findAll();
    }

    // ======= VALIDATION TESTS =======
    @Test
    void transfer_InvalidAmount() throws Exception {
        // Arrange
        transferRequest.setAmount(0.0);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(2)).findByUserName("user"); // JWT filter + service
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void topUp_InvalidAmount() throws Exception {
        // Arrange
        topUpRequest.setAmount(-100.0);
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));

        // Act & Assert
        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isBadRequest());

        verify(userRepository, times(2)).findByUserName("admin"); // JWT filter + service
        verifyNoMoreInteractions(userRepository);
    }
} 