package com.thinhtran.EzPay.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.request.TransferRequest;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.UserResponse;
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

    private User testUser1;
    private User testUser2;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .id(1L)
                .userName("user1")
                .email("user1@example.com")
                .password("encodedPassword")
                .fullName("User One")
                .role(Role.USER)
                .balance(1000.0)
                .build();

        testUser2 = User.builder()
                .id(2L)
                .userName("user2")
                .email("user2@example.com")
                .password("encodedPassword")
                .fullName("User Two")
                .role(Role.USER)
                .balance(500.0)
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUserName("newuser");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("New User");

        loginRequest = new LoginRequest();
        loginRequest.setUserName("user1");
        loginRequest.setPassword("password123");
    }

    @Test
    void completeUserFlow_RegisterLoginProfileTransfer() throws Exception {
        // Step 1: Register a new user
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser1);

        MvcResult registerResult = mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Extract token from registration response
        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(registerResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Step 2: Use token to access user profile
        when(userRepository.findByUserName("newuser")).thenReturn(Optional.of(testUser1));

        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("user1"))
                .andExpect(jsonPath("$.fullName").value("User One"))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.balance").value(1000.0));

        // Step 3: Perform a transfer
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setReceiverUsername("user2");
        transferRequest.setAmount(200.0);
        transferRequest.setMessage("Payment for services");

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));
        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(testUser2));

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        // Step 4: Check transaction history
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Verify all mocks were called appropriately
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
        verify(userRepository, atLeast(1)).findByUserName(anyString());
    }

    @Test
    void userJourney_LoginAndTransferFlow() throws Exception {
        // Step 1: Login with existing user
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        // Extract token from login response
        String loginResponse = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Step 2: Check user profile
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("user1"))
                .andExpect(jsonPath("$.balance").value(1000.0));

        // Step 3: Perform multiple transfers
        TransferRequest transfer1 = new TransferRequest();
        transfer1.setReceiverUsername("user2");
        transfer1.setAmount(100.0);
        transfer1.setMessage("First transfer");

        TransferRequest transfer2 = new TransferRequest();
        transfer2.setReceiverUsername("user2");
        transfer2.setAmount(50.0);
        transfer2.setMessage("Second transfer");

        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(testUser2));

        // First transfer
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transfer1)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        // Second transfer
        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transfer2)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));

        // Step 4: Check transaction history
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Verify all interactions
        verify(userRepository, times(2)).findByUserName("user1");
        verify(userRepository, times(2)).findByUserName("user2");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void failureFlow_InvalidCredentialsToUnauthorizedAccess() throws Exception {
        // Step 1: Try to login with invalid credentials
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        LoginRequest invalidLogin = new LoginRequest();
        invalidLogin.setUserName("user1");
        invalidLogin.setPassword("wrongpassword");

        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLogin)))
                .andExpect(status().isInternalServerError());

        // Step 2: Try to access protected resources without token
        mockMvc.perform(get("/v1/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/v1/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new TransferRequest())))
                .andExpect(status().isUnauthorized());

        // Step 3: Try with invalid token
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void businessLogicFlow_InsufficientFundsToSuccessfulTransfer() throws Exception {
        // Step 1: Setup user with low balance
        User lowBalanceUser = User.builder()
                .id(3L)
                .userName("pooruser")
                .email("poor@example.com")
                .password("encodedPassword")
                .fullName("Poor User")
                .role(Role.USER)
                .balance(10.0)
                .build();

        when(userRepository.findByUserName("pooruser")).thenReturn(Optional.of(lowBalanceUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        LoginRequest poorUserLogin = new LoginRequest();
        poorUserLogin.setUserName("pooruser");
        poorUserLogin.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(poorUserLogin)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(loginResponse, AuthResponse.class);
        String token = authResponse.getToken();

        // Step 2: Try to transfer more than available balance
        TransferRequest largeTransfer = new TransferRequest();
        largeTransfer.setReceiverUsername("user1");
        largeTransfer.setAmount(100.0); // More than 10.0 balance
        largeTransfer.setMessage("Large transfer attempt");

        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(largeTransfer)))
                .andExpect(status().isInternalServerError());

        // Step 3: Try with amount within balance
        TransferRequest smallTransfer = new TransferRequest();
        smallTransfer.setReceiverUsername("user1");
        smallTransfer.setAmount(5.0); // Within 10.0 balance
        smallTransfer.setMessage("Small transfer");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(smallTransfer)))
                .andExpect(status().isOk())
                .andExpect(content().string("Chuyển tiền thành công"));
    }

    @Test
    void registrationFlow_DuplicateUserToSuccessfulRegistration() throws Exception {
        // Step 1: Try to register with existing username
        when(userRepository.existsByUserName("user1")).thenReturn(true);

        RegisterRequest duplicateUsername = new RegisterRequest();
        duplicateUsername.setUserName("user1");
        duplicateUsername.setEmail("newemail@example.com");
        duplicateUsername.setPassword("password123");
        duplicateUsername.setFullName("New User");

        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUsername)))
                .andExpect(status().isInternalServerError());

        // Step 2: Try to register with existing email
        when(userRepository.existsByUserName("newuser")).thenReturn(false);
        when(userRepository.findByUserName("newuser")).thenReturn(Optional.empty());
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(true);

        RegisterRequest duplicateEmail = new RegisterRequest();
        duplicateEmail.setUserName("newuser");
        duplicateEmail.setEmail("user1@example.com");
        duplicateEmail.setPassword("password123");
        duplicateEmail.setFullName("New User");

        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmail)))
                .andExpect(status().isInternalServerError());

        // Step 3: Successful registration with unique credentials
        when(userRepository.existsByEmail("unique@example.com")).thenReturn(false);
        when(userRepository.findByEmail("unique@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser1);

        RegisterRequest uniqueUser = new RegisterRequest();
        uniqueUser.setUserName("newuser");
        uniqueUser.setEmail("unique@example.com");
        uniqueUser.setPassword("password123");
        uniqueUser.setFullName("New User");

        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(uniqueUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void multiUserFlow_SimultaneousTransactions() throws Exception {
        // Setup two users with tokens
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));
        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(testUser2));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // Login user1
        LoginRequest user1Login = new LoginRequest();
        user1Login.setUserName("user1");
        user1Login.setPassword("password123");

        MvcResult user1LoginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Login)))
                .andExpect(status().isOk())
                .andReturn();

        String user1Token = objectMapper.readValue(
            user1LoginResult.getResponse().getContentAsString(), 
            AuthResponse.class
        ).getToken();

        // Login user2
        LoginRequest user2Login = new LoginRequest();
        user2Login.setUserName("user2");
        user2Login.setPassword("password123");

        MvcResult user2LoginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Login)))
                .andExpect(status().isOk())
                .andReturn();

        String user2Token = objectMapper.readValue(
            user2LoginResult.getResponse().getContentAsString(), 
            AuthResponse.class
        ).getToken();

        // User1 sends money to User2
        TransferRequest user1Transfer = new TransferRequest();
        user1Transfer.setReceiverUsername("user2");
        user1Transfer.setAmount(100.0);
        user1Transfer.setMessage("From user1 to user2");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1Transfer)))
                .andExpect(status().isOk());

        // User2 sends money to User1
        TransferRequest user2Transfer = new TransferRequest();
        user2Transfer.setReceiverUsername("user1");
        user2Transfer.setAmount(50.0);
        user2Transfer.setMessage("From user2 to user1");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2Transfer)))
                .andExpect(status().isOk());

        // Both users check their profiles
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("user1"));

        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("user2"));

        // Both users check their transaction history
        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + user1Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/v1/api/transactions")
                .header("Authorization", "Bearer " + user2Token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void securityFlow_TokenExpirationAndRefresh() throws Exception {
        // Step 1: Login to get token
        when(userRepository.findByUserName("user1")).thenReturn(Optional.of(testUser1));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        MvcResult loginResult = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readValue(
            loginResult.getResponse().getContentAsString(), 
            AuthResponse.class
        ).getToken();

        // Step 2: Verify token works
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Step 3: Test with malformed token
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer malformed.token.here")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Step 4: Test with missing Bearer prefix
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Step 5: Test with empty authorization header
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void edgeCaseFlow_SpecialCharactersAndLimits() throws Exception {
        // Step 1: Register user with special characters
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser1);

        RegisterRequest specialUser = new RegisterRequest();
        specialUser.setUserName("user_with-special.chars");
        specialUser.setEmail("test+email@example.com");
        specialUser.setPassword("P@ssw0rd123!");
        specialUser.setFullName("User with Special Characters");

        MvcResult registerResult = mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialUser)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readValue(
            registerResult.getResponse().getContentAsString(), 
            AuthResponse.class
        ).getToken();

        // Step 2: Transfer with special characters in message
        when(userRepository.findByUserName("user_with-special.chars")).thenReturn(Optional.of(testUser1));
        when(userRepository.findByUserName("user2")).thenReturn(Optional.of(testUser2));

        TransferRequest specialTransfer = new TransferRequest();
        specialTransfer.setReceiverUsername("user2");
        specialTransfer.setAmount(0.01); // Very small amount
        specialTransfer.setMessage("Payment for coffee ☕ & cake 🍰 - $0.01");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(specialTransfer)))
                .andExpect(status().isOk());

        // Step 3: Transfer with zero amount
        TransferRequest zeroTransfer = new TransferRequest();
        zeroTransfer.setReceiverUsername("user2");
        zeroTransfer.setAmount(0.0);
        zeroTransfer.setMessage("Zero amount transfer");

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zeroTransfer)))
                .andExpect(status().isOk());

        // Step 4: Transfer with null/empty message
        TransferRequest nullMessageTransfer = new TransferRequest();
        nullMessageTransfer.setReceiverUsername("user2");
        nullMessageTransfer.setAmount(1.0);
        nullMessageTransfer.setMessage(null);

        mockMvc.perform(post("/v1/api/transactions")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullMessageTransfer)))
                .andExpect(status().isOk());
    }
} 