package com.thinhtran.EzPay.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "jwt.secret=testSecretKeyForJWTThatIsAtLeast256BitsLong!",
    "jwt.expirationMs=86400000"
})
class AuthControllerTest {

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

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUserName("testuser");
        loginRequest.setPassword("password123");

        testUser = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(Role.USER)
                .balance(0.0)
                .build();
    }

    @Test
    void register_Success() throws Exception {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString());

        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() throws Exception {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).existsByUserName("testuser");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_EmailAlreadyExists() throws Exception {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void register_InvalidRequestBody() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_EmptyRequestBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_MalformedJson() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_Success() throws Exception {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.token").isString());

        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void login_UserNotFound() throws Exception {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).findByUserName("testuser");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_InvalidPassword() throws Exception {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void login_EmptyUsername() throws Exception {
        // Arrange
        loginRequest.setUserName("");

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_EmptyPassword() throws Exception {
        // Arrange
        loginRequest.setPassword("");

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void login_NullCredentials() throws Exception {
        // Arrange
        LoginRequest nullRequest = new LoginRequest();
        nullRequest.setUserName(null);
        nullRequest.setPassword(null);

        // Act & Assert
        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRequest)))
                .andExpect(status().isInternalServerError());

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_CreatesValidJwtToken() throws Exception {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        var result = mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);
        
        // Verify JWT token is valid
        assertTrue(jwtTokenProvider.validateToken(authResponse.getToken()));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(authResponse.getToken()));
    }

    @Test
    void login_CreatesValidJwtToken() throws Exception {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        var result = mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        AuthResponse authResponse = objectMapper.readValue(responseContent, AuthResponse.class);
        
        // Verify JWT token is valid
        assertTrue(jwtTokenProvider.validateToken(authResponse.getToken()));
        assertEquals("testuser", jwtTokenProvider.getUsernameFromToken(authResponse.getToken()));
    }

    @Test
    void authEndpoints_ArePubliclyAccessible() throws Exception {
        // These endpoints should be accessible without authentication
        mockMvc.perform(post("/v1/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError()); // Bad request due to validation, not unauthorized

        mockMvc.perform(post("/v1/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isInternalServerError()); // Bad request due to validation, not unauthorized
    }

    private void assertTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected true but was false");
        }
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + " but was " + actual);
        }
    }
} 