package com.thinhtran.EzPay.service.impl;

import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.OTPRequest;
import com.thinhtran.EzPay.dto.request.OTPVerifyRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.OTPResponse;
import com.thinhtran.EzPay.dto.response.OTPVerifyResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.AuthenticationException;
import com.thinhtran.EzPay.exception.DuplicateDataException;
import com.thinhtran.EzPay.exception.UserNotFoundException;
import com.thinhtran.EzPay.exception.ValidationException;
import com.thinhtran.EzPay.repository.UserRepository;
import com.thinhtran.EzPay.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
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
    private JwtTokenProvider jwtProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private ChangePasswordRequest changePasswordRequest;
    private OTPRequest otpRequest;
    private OTPVerifyRequest otpVerifyRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUserName("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUserName("testuser");
        loginRequest.setPassword("Password123!");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("Password123!");
        changePasswordRequest.setNewPassword("NewPassword456!");

        otpRequest = new OTPRequest();
        otpRequest.setPhoneNumber("0901234567");

        otpVerifyRequest = new OTPVerifyRequest();
        otpVerifyRequest.setPhoneNumber("0901234567");
        otpVerifyRequest.setOtp("123456");

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

    // ======= REGISTER TESTS =======
    @Test
    void register_Success() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtProvider.generateToken(anyString())).thenReturn("jwt_token");

        // Act
        AuthResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("jwt_token", result.getToken());
        
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("Password123!");
        verify(jwtProvider).generateToken("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        // Act & Assert
        DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username 'testuser' already exists", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtProvider);
    }

    @Test
    void register_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email 'test@example.com' already exists", exception.getMessage());
        verify(userRepository).existsByUserName("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verifyNoMoreInteractions(passwordEncoder, jwtProvider);
    }

    @Test
    void register_CreatesUserWithCorrectDefaults() {
        // Arrange
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtProvider.generateToken(anyString())).thenReturn("jwt_token");

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

    // ======= LOGIN TESTS =======
    @Test
    void login_Success() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateToken(anyString())).thenReturn("jwt_token");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt_token", response.getToken());
        
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
        verify(jwtProvider).generateToken("testuser");
    }

    @Test
    void login_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verifyNoMoreInteractions(passwordEncoder, jwtProvider);
    }

    @Test
    void login_InvalidPassword() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
        verifyNoMoreInteractions(jwtProvider);
    }

    // ======= CHANGE PASSWORD TESTS =======
    @Test
    void changePassword_Success() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456!", "encodedPassword")).thenReturn(false);
        when(passwordEncoder.encode("NewPassword456!")).thenReturn("newEncodedPassword");

        // Act
        authService.changePassword("testuser", changePasswordRequest);

        // Assert
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
        verify(passwordEncoder).matches("NewPassword456!", "encodedPassword");
        verify(passwordEncoder).encode("NewPassword456!");
        verify(userRepository).save(testUser);
        assertEquals("newEncodedPassword", testUser.getPassword());
    }

    @Test
    void changePassword_UserNotFound() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authService.changePassword("nonexistent", changePasswordRequest);
        });

        assertEquals("User not found: nonexistent", exception.getMessage());
        verify(userRepository).findByUserName("nonexistent");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.changePassword("testuser", changePasswordRequest);
        });

        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
        verifyNoMoreInteractions(passwordEncoder);
    }

    @Test
    void changePassword_NewPasswordSameAsCurrent() {
        // Arrange
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.matches("NewPassword456!", "encodedPassword")).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authService.changePassword("testuser", changePasswordRequest);
        });

        assertEquals("New password must be different from current password", exception.getMessage());
        verify(userRepository).findByUserName("testuser");
        verify(passwordEncoder).matches("Password123!", "encodedPassword");
        verify(passwordEncoder).matches("NewPassword456!", "encodedPassword");
        verifyNoMoreInteractions(passwordEncoder);
    }

    // ======= OTP TESTS =======
    @Test
    void generateOTP_Success() {
        // Act
        OTPResponse response = authService.generateOTP(otpRequest);

        // Assert
        assertNotNull(response);
        assertEquals("OTP đã được gửi đến số điện thoại 0901234567", response.getMessage());
        
        // Verify OTP is stored in internal storage
        @SuppressWarnings("unchecked")
        Map<String, String> otpStorage = (Map<String, String>) ReflectionTestUtils.getField(authService, "otpStorage");
        assertTrue(otpStorage.containsKey("0901234567"));
        String storedOtp = otpStorage.get("0901234567");
        assertNotNull(storedOtp);
        assertEquals(6, storedOtp.length());
        assertTrue(storedOtp.matches("\\d{6}"));
    }

    @Test
    void generateOTP_DifferentPhoneNumbers() {
        // Arrange
        OTPRequest request1 = new OTPRequest();
        request1.setPhoneNumber("0901111111");
        
        OTPRequest request2 = new OTPRequest();
        request2.setPhoneNumber("0902222222");

        // Act
        OTPResponse response1 = authService.generateOTP(request1);
        OTPResponse response2 = authService.generateOTP(request2);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("OTP đã được gửi đến số điện thoại 0901111111", response1.getMessage());
        assertEquals("OTP đã được gửi đến số điện thoại 0902222222", response2.getMessage());
        
        @SuppressWarnings("unchecked")
        Map<String, String> otpStorage = (Map<String, String>) ReflectionTestUtils.getField(authService, "otpStorage");
        assertTrue(otpStorage.containsKey("0901111111"));
        assertTrue(otpStorage.containsKey("0902222222"));
        assertNotEquals(otpStorage.get("0901111111"), otpStorage.get("0902222222"));
    }

    @Test
    void verifyOTP_Success() {
        // Arrange - First generate an OTP
        authService.generateOTP(otpRequest);
        
        // Get the generated OTP from storage
        @SuppressWarnings("unchecked")
        Map<String, String> otpStorage = (Map<String, String>) ReflectionTestUtils.getField(authService, "otpStorage");
        String generatedOtp = otpStorage.get("0901234567");
        
        otpVerifyRequest.setOtp(generatedOtp);

        // Act
        OTPVerifyResponse response = authService.verifyOTP(otpVerifyRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isValid());
        
        // Verify OTP is removed after successful verification
        assertFalse(otpStorage.containsKey("0901234567"));
    }

    @Test
    void verifyOTP_WithDemoOTP() {
        // Arrange - Use demo OTP without generating
        otpVerifyRequest.setOtp("123456");

        // Act
        OTPVerifyResponse response = authService.verifyOTP(otpVerifyRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isValid());
    }

    @Test
    void verifyOTP_InvalidOTP() {
        // Arrange - Generate OTP but verify with wrong code
        authService.generateOTP(otpRequest);
        otpVerifyRequest.setOtp("999999");

        // Act
        OTPVerifyResponse response = authService.verifyOTP(otpVerifyRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
        
        // Verify OTP is still in storage (not removed on failed verification)
        @SuppressWarnings("unchecked")
        Map<String, String> otpStorage = (Map<String, String>) ReflectionTestUtils.getField(authService, "otpStorage");
        assertTrue(otpStorage.containsKey("0901234567"));
    }

    @Test
    void verifyOTP_NoOTPGenerated() {
        // Arrange - Try to verify without generating OTP first
        otpVerifyRequest.setOtp("999999"); // Use non-demo OTP

        // Act
        OTPVerifyResponse response = authService.verifyOTP(otpVerifyRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }

    @Test
    void verifyOTP_DifferentPhoneNumber() {
        // Arrange - Generate OTP for one number but verify for another
        authService.generateOTP(otpRequest);
        
        @SuppressWarnings("unchecked")
        Map<String, String> otpStorage = (Map<String, String>) ReflectionTestUtils.getField(authService, "otpStorage");
        String generatedOtp = otpStorage.get("0901234567");
        
        otpVerifyRequest.setPhoneNumber("0987654321");
        otpVerifyRequest.setOtp(generatedOtp);

        // Act
        OTPVerifyResponse response = authService.verifyOTP(otpVerifyRequest);

        // Assert
        assertNotNull(response);
        assertFalse(response.isValid());
    }
} 