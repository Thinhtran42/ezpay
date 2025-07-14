package com.thinhtran.EzPay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTThatIsAtLeast256BitsLong!",
    "jwt.expirationMs=86400000"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User regularUser;
    private User adminUser;
    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        regularUser = User.builder()
                .id(1L)
                .userName("user")
                .email("user@example.com")
                .fullName("Regular User")
                .password("encoded_password")
                .balance(1000.0)
                .role(Role.USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .userName("admin")
                .email("admin@example.com")
                .fullName("Admin User")
                .password("encoded_password")
                .balance(5000.0)
                .role(Role.ADMIN)
                .build();

        userToken = jwtTokenProvider.generateToken("user");
        adminToken = jwtTokenProvider.generateToken("admin");
        
        // Mock the JWT authentication user lookup
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));
    }

    // ======= GET ME TESTS =======
    @Test
    void getMe_Success() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.userName").value("user"))
                .andExpect(jsonPath("$.data.fullName").value("Regular User"))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.balance").value(1000.0))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void getMe_WithAdminRole() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.userName").value("admin"))
                .andExpect(jsonPath("$.data.fullName").value("Admin User"))
                .andExpect(jsonPath("$.data.email").value("admin@example.com"))
                .andExpect(jsonPath("$.data.balance").value(5000.0))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    void getMe_WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithInvalidToken() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithMalformedAuthHeader() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "InvalidFormat")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithMissingBearerPrefix() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", userToken) // Missing "Bearer " prefix
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_UserNotFoundInDatabase() throws Exception {
        String tokenForNonExistentUser = jwtTokenProvider.generateToken("nonexistent");
        
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + tokenForNonExistentUser)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_WithZeroBalance() throws Exception {
        regularUser.setBalance(0.0);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.balance").value(0.0));
    }

    @Test
    void getMe_WithNullFullName() throws Exception {
        regularUser.setFullName(null);
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(regularUser));

        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.fullName").value(nullValue()));
    }

    @Test
    void getMe_ReturnsOnlyAllowedFields() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.userName").exists())
                .andExpect(jsonPath("$.data.fullName").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.balance").exists())
                .andExpect(jsonPath("$.data.role").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void getMe_ContentTypeVerification() throws Exception {
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Lấy thông tin người dùng thành công"))
                .andExpect(jsonPath("$.data.userName").value("user"));
    }

    @Test
    void getMe_MultipleCallsWithSameToken() throws Exception {
        // First call
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("user"));

        // Second call with same token should work
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userName").value("user"));
    }
} 