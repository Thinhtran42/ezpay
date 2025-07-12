package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

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
    void register_Success() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt_token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken("testuser");
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verifyNoMoreInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_UsernameAlreadyInUse() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("username already in use", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verifyNoMoreInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("email already exists", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoMoreInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void register_EmailAlreadyInUse() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("email already in use", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).findByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).findByEmail("test@example.com");
        verifyNoMoreInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt_token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtTokenProvider).generateToken("testuser");
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verifyNoMoreInteractions(passwordEncoder, jwtTokenProvider);
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verifyNoMoreInteractions(jwtTokenProvider);
    }

    @Test
    void register_CreatesUserWithCorrectDefaults() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtTokenProvider.generateToken(anyString())).thenReturn("jwt_token");

        // Act
        authService.register(registerRequest);

        // Assert
        verify(userRepository).save(argThat(user -> 
            user.getUserName().equals("testuser") &&
            user.getEmail().equals("test@example.com") &&
            user.getPassword().equals("encodedPassword") &&
            user.getFullName().equals("Test User") &&
            user.getRole().equals(Role.USER) &&
            user.getBalance().equals(0.0)
        ));
    }
} 