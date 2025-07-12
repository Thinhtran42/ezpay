package com.thinhtran.EzPay.security;

import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import com.thinhtran.EzPay.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext();
        
        testUser = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .role(Role.USER)
                .balance(1000.0)
                .build();
    }

    @Test
    void doFilterInternal_PublicAuthPath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_SwaggerPath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ApiDocsPath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v3/api-docs");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_NoAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidAuthorizationHeader() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Invalid header");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_InvalidToken() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer invalid_token");
        when(jwtTokenProvider.validateToken("invalid_token")).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken("invalid_token");
        verifyNoInteractions(userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenUserNotFound() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.empty());

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUsernameFromToken(validToken);
        verify(userRepository).findByUserName("testuser");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_ValidTokenAndUser() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUsernameFromToken(validToken);
        verify(userRepository).findByUserName("testuser");
        
        // Check authentication was set
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(testUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Test
    void doFilterInternal_BearerTokenWithSpaces() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer  " + validToken); // Extra space
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUsernameFromToken(validToken);
        verify(userRepository).findByUserName("testuser");
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_EmptyBearerToken() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer ");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_OnlyBearer() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_CaseSensitiveBearerCheck() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("bearer valid_token");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_TokenValidationThrowsException() throws ServletException, IOException {
        // Arrange
        String invalidToken = "invalid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + invalidToken);
        when(jwtTokenProvider.validateToken(invalidToken)).thenThrow(new RuntimeException("Token validation failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(invalidToken);
        verifyNoInteractions(userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_GetUsernameFromTokenThrowsException() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenThrow(new RuntimeException("Username extraction failed"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUsernameFromToken(validToken);
        verifyNoInteractions(userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_UserRepositoryThrowsException() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenThrow(new RuntimeException("Database error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider).validateToken(validToken);
        verify(jwtTokenProvider).getUsernameFromToken(validToken);
        verify(userRepository).findByUserName("testuser");
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_MultiplePathChecks() throws ServletException, IOException {
        // Test various auth paths
        String[] authPaths = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/logout"
        };

        for (String path : authPaths) {
            // Arrange
            SecurityContextHolder.clearContext();
            when(request.getRequestURI()).thenReturn(path);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, times(1)).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void doFilterInternal_SwaggerPaths() throws ServletException, IOException {
        // Test various swagger paths
        String[] swaggerPaths = {
            "/swagger-ui/index.html",
            "/swagger-ui/swagger-ui.css",
            "/swagger-ui.html"
        };

        for (String path : swaggerPaths) {
            // Arrange
            SecurityContextHolder.clearContext();
            when(request.getRequestURI()).thenReturn(path);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, times(1)).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void doFilterInternal_ApiDocsPaths() throws ServletException, IOException {
        // Test various api-docs paths
        String[] apiDocsPaths = {
            "/v3/api-docs",
            "/v3/api-docs/swagger-config",
            "/v3/api-docs.yaml"
        };

        for (String path : apiDocsPaths) {
            // Arrange
            SecurityContextHolder.clearContext();
            when(request.getRequestURI()).thenReturn(path);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(filterChain, times(1)).doFilter(request, response);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }
    }

    @Test
    void doFilterInternal_AuthenticationDetails() throws ServletException, IOException {
        // Arrange
        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("testuser");
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(testUser));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(testUser, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertNull(authentication.getAuthorities());
        assertNotNull(authentication.getDetails());
    }

    @Test
    void doFilterInternal_DifferentUserRoles() throws ServletException, IOException {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .userName("admin")
                .email("admin@example.com")
                .password("password")
                .fullName("Admin User")
                .role(Role.ADMIN)
                .balance(0.0)
                .build();

        String validToken = "valid_token";
        when(request.getRequestURI()).thenReturn("/v1/api/users/me");
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + validToken);
        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(validToken)).thenReturn("admin");
        when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(adminUser, authentication.getPrincipal());
        assertEquals(Role.ADMIN, ((User) authentication.getPrincipal()).getRole());
    }

    @Test
    void doFilterInternal_EmptyPath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_NullPath() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtTokenProvider, userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
} 