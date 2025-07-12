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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private User testUser;
    private String validToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(Role.USER)
                .balance(1000.0)
                .phone("123456789")
                .build();

        validToken = jwtTokenProvider.generateToken("testuser");
    }

    @Test
    void getMe_Success() throws Exception {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithoutAuthentication() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getMe_WithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer invalid_token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getMe_WithExpiredToken() throws Exception {
        // Arrange - Create a token that should be expired (this is a simulation)
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.invalid";

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getMe_WithMalformedAuthHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "InvalidFormat")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getMe_WithMissingBearerPrefix() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository);
    }

    @Test
    void getMe_UserNotFoundInDatabase() throws Exception {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithZeroBalance() throws Exception {
        // Arrange
        testUser.setBalance(0.0);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(0.0));

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithNullFullName() throws Exception {
        // Arrange
        testUser.setFullName(null);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").isEmpty());

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithLargeBalance() throws Exception {
        // Arrange
        testUser.setBalance(999999.99);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(999999.99));

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_ReturnsOnlyAllowedFields() throws Exception {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").exists())
                .andExpect(jsonPath("$.fullName").exists())
                .andExpect(jsonPath("$.email").exists())
                .andExpect(jsonPath("$.balance").exists())
                // Ensure sensitive fields are not exposed
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.role").doesNotExist())
                .andExpect(jsonPath("$.phone").doesNotExist());

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithDifferentRoles() throws Exception {
        // Arrange - Test with ADMIN role
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"))
                .andExpect(jsonPath("$.role").doesNotExist()); // Role should not be exposed

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithSpecialCharactersInName() throws Exception {
        // Arrange
        testUser.setFullName("Test User with Special Chars!@#$%^&*()");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test User with Special Chars!@#$%^&*()"));

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_WithEmptyStringValues() throws Exception {
        // Arrange
        testUser.setFullName("");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(""));

        verify(userRepository).findByUserName("testuser");
    }

    @Test
    void getMe_MultipleCallsWithSameToken() throws Exception {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert - First call
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"));

        // Act & Assert - Second call
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("testuser"));

        verify(userRepository, times(2)).findByUserName("testuser");
    }

    @Test
    void getMe_ContentTypeVerification() throws Exception {
        // Arrange
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/v1/api/users/me")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userName").value("testuser"));

        verify(userRepository).findByUserName("testuser");
    }
} 