package com.thinhtran.EzPay.controller;

import com.thinhtran.EzPay.dto.request.ChangePasswordRequest;
import com.thinhtran.EzPay.dto.request.LoginRequest;
import com.thinhtran.EzPay.dto.request.OTPRequest;
import com.thinhtran.EzPay.dto.request.OTPVerifyRequest;
import com.thinhtran.EzPay.dto.request.RegisterRequest;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import com.thinhtran.EzPay.dto.response.AuthResponse;
import com.thinhtran.EzPay.dto.response.OTPResponse;
import com.thinhtran.EzPay.dto.response.OTPVerifyResponse;
import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.exception.*;
import com.thinhtran.EzPay.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

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
        registerRequest.setPassword("TestPassword123!");
        registerRequest.setEmail("test@example.com");
        registerRequest.setFullName("Test User");

        loginRequest = new LoginRequest();
        loginRequest.setUserName("testuser");
        loginRequest.setPassword("TestPassword123!");

        changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setCurrentPassword("TestPassword123!");
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
                .fullName("Test User")
                .phone("0123456789")
                .role(Role.USER)
                .balance(0.0)
                .build();
    }

    // ======= REGISTER TESTS =======
    @Test
    void register_Success() {
        // Given
        AuthResponse authResponse = new AuthResponse("test-jwt-token");
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(registerRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Đăng ký thành công", response.getBody().getMessage());
        assertEquals("test-jwt-token", response.getBody().getData().getToken());
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateDataException("Username", "testuser"));

        // When & Then
        DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> {
            authController.register(registerRequest);
        });
        
        assertEquals("Username 'testuser' already exists", exception.getMessage());
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void register_EmailAlreadyExists() {
        // Given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateDataException("Email", "test@example.com"));

        // When & Then
        DuplicateDataException exception = assertThrows(DuplicateDataException.class, () -> {
            authController.register(registerRequest);
        });
        
        assertEquals("Email 'test@example.com' already exists", exception.getMessage());
        verify(authService, times(1)).register(registerRequest);
    }

    // ======= LOGIN TESTS =======
    @Test
    void login_Success() {
        // Given
        AuthResponse authResponse = new AuthResponse("test-jwt-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(loginRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Đăng nhập thành công", response.getBody().getMessage());
        assertEquals("test-jwt-token", response.getBody().getData().getToken());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void login_UserNotFound() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authController.login(loginRequest);
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void login_InvalidPassword() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthenticationException("Invalid username or password"));

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authController.login(loginRequest);
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        verify(authService, times(1)).login(loginRequest);
    }

    // ======= CHANGE PASSWORD TESTS =======
    @Test
    void changePassword_Success() {
        // Given
        doNothing().when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        // When
        ResponseEntity<ApiResponse<Void>> response = authController.changePassword(testUser, changePasswordRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Đổi mật khẩu thành công", response.getBody().getMessage());
        assertNull(response.getBody().getData());
        verify(authService, times(1)).changePassword("testuser", changePasswordRequest);
    }

    @Test
    void changePassword_UserNotFound() {
        // Given
        doThrow(new UserNotFoundException("testuser"))
                .when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        // When & Then
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authController.changePassword(testUser, changePasswordRequest);
        });
        
        assertEquals("User not found: testuser", exception.getMessage());
        verify(authService, times(1)).changePassword("testuser", changePasswordRequest);
    }

    @Test
    void changePassword_IncorrectCurrentPassword() {
        // Given
        doThrow(new AuthenticationException("Current password is incorrect"))
                .when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        // When & Then
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authController.changePassword(testUser, changePasswordRequest);
        });
        
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(authService, times(1)).changePassword("testuser", changePasswordRequest);
    }

    @Test
    void changePassword_NewPasswordSameAsCurrent() {
        // Given
        doThrow(new ValidationException("New password must be different from current password"))
                .when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        // When & Then
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            authController.changePassword(testUser, changePasswordRequest);
        });
        
        assertEquals("New password must be different from current password", exception.getMessage());
        verify(authService, times(1)).changePassword("testuser", changePasswordRequest);
    }

    // ======= GENERATE OTP TESTS =======
    @Test
    void generateOTP_Success() {
        // Given
        OTPResponse otpResponse = new OTPResponse("OTP đã được gửi đến số điện thoại 0901234567");
        when(authService.generateOTP(any(OTPRequest.class))).thenReturn(otpResponse);

        // When
        ResponseEntity<ApiResponse<OTPResponse>> response = authController.generateOTP(otpRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Tạo OTP thành công", response.getBody().getMessage());
        assertEquals("OTP đã được gửi đến số điện thoại 0901234567", response.getBody().getData().getMessage());
        verify(authService, times(1)).generateOTP(otpRequest);
    }

    @Test
    void generateOTP_WithDifferentPhoneNumber() {
        // Given
        OTPRequest differentRequest = new OTPRequest();
        differentRequest.setPhoneNumber("0987654321");
        OTPResponse otpResponse = new OTPResponse("OTP đã được gửi đến số điện thoại 0987654321");
        when(authService.generateOTP(any(OTPRequest.class))).thenReturn(otpResponse);

        // When
        ResponseEntity<ApiResponse<OTPResponse>> response = authController.generateOTP(differentRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Tạo OTP thành công", response.getBody().getMessage());
        assertEquals("OTP đã được gửi đến số điện thoại 0987654321", response.getBody().getData().getMessage());
        verify(authService, times(1)).generateOTP(differentRequest);
    }

    // ======= VERIFY OTP TESTS =======
    @Test
    void verifyOTP_Success() {
        // Given
        OTPVerifyResponse otpVerifyResponse = new OTPVerifyResponse(true);
        when(authService.verifyOTP(any(OTPVerifyRequest.class))).thenReturn(otpVerifyResponse);

        // When
        ResponseEntity<ApiResponse<OTPVerifyResponse>> response = authController.verifyOTP(otpVerifyRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Xác thực OTP thành công", response.getBody().getMessage());
        assertTrue(response.getBody().getData().isValid());
        verify(authService, times(1)).verifyOTP(otpVerifyRequest);
    }

    @Test
    void verifyOTP_InvalidOTP() {
        // Given
        OTPVerifyResponse otpVerifyResponse = new OTPVerifyResponse(false);
        when(authService.verifyOTP(any(OTPVerifyRequest.class))).thenReturn(otpVerifyResponse);

        // When
        ResponseEntity<ApiResponse<OTPVerifyResponse>> response = authController.verifyOTP(otpVerifyRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Xác thực OTP thành công", response.getBody().getMessage());
        assertFalse(response.getBody().getData().isValid());
        verify(authService, times(1)).verifyOTP(otpVerifyRequest);
    }

    @Test
    void verifyOTP_WithWrongOTP() {
        // Given
        OTPVerifyRequest wrongRequest = new OTPVerifyRequest();
        wrongRequest.setPhoneNumber("0901234567");
        wrongRequest.setOtp("999999");
        
        OTPVerifyResponse otpVerifyResponse = new OTPVerifyResponse(false);
        when(authService.verifyOTP(any(OTPVerifyRequest.class))).thenReturn(otpVerifyResponse);

        // When
        ResponseEntity<ApiResponse<OTPVerifyResponse>> response = authController.verifyOTP(wrongRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().getCode());
        assertEquals("Xác thực OTP thành công", response.getBody().getMessage());
        assertFalse(response.getBody().getData().isValid());
        verify(authService, times(1)).verifyOTP(wrongRequest);
    }

    // ======= EXCEPTION HANDLING TESTS =======
    @Test
    void register_HandlesGenericException() {
        // Given
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.register(registerRequest);
        });
        
        assertEquals("Database error", exception.getMessage());
        verify(authService, times(1)).register(registerRequest);
    }

    @Test
    void login_HandlesGenericException() {
        // Given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.login(loginRequest);
        });
        
        assertEquals("Service unavailable", exception.getMessage());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void changePassword_HandlesGenericException() {
        // Given
        doThrow(new RuntimeException("Unexpected error"))
                .when(authService).changePassword(anyString(), any(ChangePasswordRequest.class));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.changePassword(testUser, changePasswordRequest);
        });
        
        assertEquals("Unexpected error", exception.getMessage());
        verify(authService, times(1)).changePassword("testuser", changePasswordRequest);
    }

    @Test
    void generateOTP_HandlesGenericException() {
        // Given
        when(authService.generateOTP(any(OTPRequest.class)))
                .thenThrow(new RuntimeException("SMS service unavailable"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.generateOTP(otpRequest);
        });
        
        assertEquals("SMS service unavailable", exception.getMessage());
        verify(authService, times(1)).generateOTP(otpRequest);
    }

    @Test
    void verifyOTP_HandlesGenericException() {
        // Given
        when(authService.verifyOTP(any(OTPVerifyRequest.class)))
                .thenThrow(new RuntimeException("Verification service error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authController.verifyOTP(otpVerifyRequest);
        });
        
        assertEquals("Verification service error", exception.getMessage());
        verify(authService, times(1)).verifyOTP(otpVerifyRequest);
    }
} 