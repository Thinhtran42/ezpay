package com.thinhtran.EzPay.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thinhtran.EzPay.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleBusinessException() {
        // Arrange
        BusinessException exception = new BusinessException("TEST_ERROR", "Test business error");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleBusinessException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TEST_ERROR", response.getBody().getCode());
        assertEquals("Test business error", response.getBody().getMessage());
    }

    @Test
    void handleValidationException() {
        // Arrange
        ValidationException exception = new ValidationException("Validation failed");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleValidationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("Validation failed", response.getBody().getMessage());
    }

    @Test
    void handleUserNotFoundException() {
        // Arrange
        UserNotFoundException exception = new UserNotFoundException("testuser");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleUserNotFoundException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("USER_NOT_FOUND", response.getBody().getCode());
        assertEquals("User not found: testuser", response.getBody().getMessage());
    }

    @Test
    void handleInsufficientBalanceException() {
        // Arrange
        InsufficientBalanceException exception = new InsufficientBalanceException(100.0, 200.0);

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleInsufficientBalanceException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INSUFFICIENT_BALANCE", response.getBody().getCode());
        assertTrue(response.getBody().getMessage().contains("Số dư không đủ"));
    }

    @Test
    void handleAuthenticationException() {
        // Arrange
        AuthenticationException exception = new AuthenticationException("Invalid credentials");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAuthenticationException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("AUTHENTICATION_ERROR", response.getBody().getCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleAccessDeniedException() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleAccessDeniedException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ACCESS_DENIED", response.getBody().getCode());
        assertEquals("Access denied", response.getBody().getMessage());
    }

    @Test
    void handleDuplicateDataException() {
        // Arrange
        DuplicateDataException exception = new DuplicateDataException("Username", "testuser");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleDuplicateDataException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_DATA", response.getBody().getCode());
        assertEquals("Username 'testuser' already exists", response.getBody().getMessage());
    }

    @Test 
    void handleValidationErrors() {
        // Test this indirectly through BindException which has similar behavior
        // but doesn't require complex mocking of MethodParameter
        
        // Arrange
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "testObject");
        bindingResult.addError(new FieldError("testObject", "username", "Username is required"));
        bindingResult.addError(new FieldError("testObject", "email", "Email is invalid"));
        
        BindException exception = new BindException(bindingResult);

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = globalExceptionHandler.handleBindException(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().containsKey("username"));
        assertTrue(response.getBody().getData().containsKey("email"));
        assertEquals("Username is required", response.getBody().getData().get("username"));
        assertEquals("Email is invalid", response.getBody().getData().get("email"));
    }

    @Test
    void handleRuntimeException() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleRuntimeException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGenericException() {
        // Arrange
        Exception exception = new Exception("Generic error");

        // Act
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler.handleGenericException(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INTERNAL_ERROR", response.getBody().getCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }

    @Test
    void getHttpStatusForErrorCode_AllCases() {
        // Test all error code mappings using reflection or direct method call if public
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        // Since the method is private, we test through business exceptions
        BusinessException validationError = new BusinessException("VALIDATION_ERROR", "test");
        ResponseEntity<ApiResponse<Object>> response1 = handler.handleBusinessException(validationError, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());

        BusinessException userNotFound = new BusinessException("USER_NOT_FOUND", "test");
        ResponseEntity<ApiResponse<Object>> response2 = handler.handleBusinessException(userNotFound, webRequest);
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());

        BusinessException authError = new BusinessException("AUTHENTICATION_ERROR", "test");
        ResponseEntity<ApiResponse<Object>> response3 = handler.handleBusinessException(authError, webRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, response3.getStatusCode());

        BusinessException accessDenied = new BusinessException("ACCESS_DENIED", "test");
        ResponseEntity<ApiResponse<Object>> response4 = handler.handleBusinessException(accessDenied, webRequest);
        assertEquals(HttpStatus.FORBIDDEN, response4.getStatusCode());

        BusinessException duplicateData = new BusinessException("DUPLICATE_DATA", "test");
        ResponseEntity<ApiResponse<Object>> response5 = handler.handleBusinessException(duplicateData, webRequest);
        assertEquals(HttpStatus.CONFLICT, response5.getStatusCode());

        BusinessException insufficientBalance = new BusinessException("INSUFFICIENT_BALANCE", "test");
        ResponseEntity<ApiResponse<Object>> response6 = handler.handleBusinessException(insufficientBalance, webRequest);
        assertEquals(HttpStatus.BAD_REQUEST, response6.getStatusCode());

        BusinessException unknownError = new BusinessException("UNKNOWN_ERROR", "test");
        ResponseEntity<ApiResponse<Object>> response7 = handler.handleBusinessException(unknownError, webRequest);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response7.getStatusCode());
    }
} 