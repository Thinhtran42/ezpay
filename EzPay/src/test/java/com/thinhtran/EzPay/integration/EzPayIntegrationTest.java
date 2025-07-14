package com.thinhtran.EzPay.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.request.TopUpRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTThatIsAtLeast256BitsLong!",
    "jwt.expirationMs=86400000"
})
class EzPayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User regularUser;
    private User adminUser;
    private User receiverUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

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

        receiverUser = User.builder()
                .id(3L)
                .userName("receiver")
                .email("receiver@example.com")
                .password("encodedPassword")
                .fullName("Receiver User")
                .role(Role.USER)
                .balance(500.0)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUserName("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("New User");

        loginRequest = new LoginRequest();
        loginRequest.setUserName("user");
        loginRequest.setPassword("Password123!");
    }

    // Helper method to extract token from ApiResponse
    private String extractTokenFromResponse(String responseContent) throws Exception {
        TypeReference<ApiResponse<AuthResponse>> typeRef = new TypeReference<ApiResponse<AuthResponse>>() {};
        ApiResponse<AuthResponse> apiResponse = objectMapper.readValue(responseContent, typeRef);
        return apiResponse.getData().getToken();
    }

    // ======= COMPLETE USER JOURNEY TESTS =======
    @Test
    void completeUserJourney_RegisterLoginProfileTransfer() throws Exception {
        // Step 1: Register a new user
        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        
        User newUser = User.builder()
                .id(4L)
                .userName("newuser")
                .email("newuser@example.com")
                .password("encodedPassword")
                .fullName("New User")
                .role(Role.USER)
                .balance(0.0)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        MvcResult registerResult = mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Đăng ký thành công"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String token = extractTokenFromResponse(registerResult.getResponse().getContentAsString());

        // Step 2: Use token to access user profile
        when(userRepository.findByUserName("newuser")).thenReturn(Optional.of(newUser));

        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.userName").value("newuser"))
                .andExpect(jsonPath("$.data.fullName").value("New User"))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.balance").value(0.0));

        // Step 3: Admin tops up user account
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setTargetUsername("newuser");
        topUpRequest.setAmount(500.0);

        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findByUserName("newuser")).thenReturn(Optional.of(newUser));
        String adminToken = jwtTokenProvider.generateToken("admin");

        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Nạp tiền thành công"));

        // Step 4: Perform a transfer
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(200.0);
        transferRequest.setMessage("Payment for services");

        newUser.setBalance(500.0); // Update balance after top-up
        when(userRepository.findByUserName("newuser")).thenReturn(Optional.of(newUser));
        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiverUser));

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Chuyển tiền thành công"));

        // Verify final balances
        assertEquals(300.0, newUser.getBalance()); // 500 - 200
        assertEquals(700.0, receiverUser.getBalance()); // 500 + 200
    }

    @Test
    void userLoginAndTransferFlow() throws Exception {
        // Step 1: Login with existing user
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Đăng nhập thành công"))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        String token = extractTokenFromResponse(loginResult.getResponse().getContentAsString());

        // Step 2: Check user profile
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("user"))
                .andExpect(jsonPath("$.data.balance").value(1000.0));

        // Step 3: Perform transfer
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(300.0);
        transferRequest.setMessage("Monthly payment");

        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiverUser));

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Chuyển tiền thành công"));

        // Step 4: Check transaction history
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray());

        // Verify final balances
        assertEquals(700.0, regularUser.getBalance()); // 1000 - 300
        assertEquals(800.0, receiverUser.getBalance()); // 500 + 300
    }

    // ======= ERROR HANDLING FLOWS =======
    @Test
    void failureFlow_InvalidCredentialsToUnauthorizedAccess() throws Exception {
        // Step 1: Try to login with invalid credentials
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setUserName("user");
        invalidLogin.setPassword("wrongpassword");

        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isUnauthorized());

        // Step 2: Try to access protected resources without token
        mockMvc.perform(get("/v1/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("receiver");
        transferRequest.setAmount(100.0);
        transferRequest.setMessage("Test");

        mockMvc.perform(post("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isUnauthorized());

        // Step 3: Try with invalid token
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void businessLogicFlow_InsufficientFundsToSuccessfulTransfer() throws Exception {
        // Step 1: Login
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = extractTokenFromResponse(loginResult.getResponse().getContentAsString());

        // Step 2: Try to transfer more money than available
        TransferRequest largeTransfer = new TransferRequest();
        largeTransfer.setReceiverUsername("receiver");
        largeTransfer.setAmount(1500.0); // More than user's 1000.0 balance
        largeTransfer.setMessage("Large transfer");

        when(userRepository.findByUserName("receiver")).thenReturn(Optional.of(receiverUser));

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeTransfer)))
                .andExpect(status().isBadRequest());

        // Step 3: Perform valid transfer within balance
        TransferRequest validTransfer = new TransferRequest();
        validTransfer.setReceiverUsername("receiver");
        validTransfer.setAmount(500.0);
        validTransfer.setMessage("Valid transfer");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validTransfer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }

    // ======= ADMIN FLOW TESTS =======
    @Test
    void adminFlow_TopUpAndStatistics() throws Exception {
        // Step 1: Admin login
        LoginRequest adminLogin = new LoginRequest();
        adminLogin.setUserName("admin");
        adminLogin.setPassword("Password123!");

        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String adminToken = extractTokenFromResponse(loginResult.getResponse().getContentAsString());

        // Step 2: Perform top-up
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setTargetUsername("user");
        topUpRequest.setAmount(1000.0);

        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Nạp tiền thành công"));

        // Step 3: Check statistics
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thống kê thành công"))
                .andExpect(jsonPath("$.data.totalTransferred").exists())
                .andExpect(jsonPath("$.data.totalTransactions").exists());
    }

    // ======= AUTHENTICATION FLOW TESTS =======
    @Test
    void authenticationFlow_ChangePassword() throws Exception {
        // Step 1: Login
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = extractTokenFromResponse(loginResult.getResponse().getContentAsString());

        // Step 2: Change password
        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("Password123!");
        changePasswordRequest.setNewPassword("NewPassword456!");

        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456!", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456!")).thenReturn("newEncodedPassword");

        mockMvc.perform(put("/v1/api/auth/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Đổi mật khẩu thành công"));

        // Verify password was updated
        assertEquals("newEncodedPassword", regularUser.getPassword());
    }

    @Test
    void securityFlow_AccessDeniedForNonAdminOperations() throws Exception {
        // Step 1: Regular user login
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String userToken = extractTokenFromResponse(loginResult.getResponse().getContentAsString());

        // Step 2: Try to access admin-only top-up endpoint
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setTargetUsername("receiver");
        topUpRequest.setAmount(500.0);

        mockMvc.perform(post("/v1/api/transactions/top-up")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topUpRequest)))
                .andExpect(status().isForbidden());

        // Step 3: Try to access admin-only statistics endpoint
        mockMvc.perform(get("/v1/api/transactions/statistics")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
} 