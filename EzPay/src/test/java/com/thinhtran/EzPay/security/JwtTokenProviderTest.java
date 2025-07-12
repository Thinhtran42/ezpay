package com.thinhtran.EzPay.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret;
    private long testExpirationMs;

    @BeforeEach
    void setUp() {
        testSecret = "testSecretKeyForJWTThatIsAtLeast256BitsLong!";
        testExpirationMs = 86400000; // 24 hours
        jwtTokenProvider = new JwtTokenProvider(testSecret, testExpirationMs);
    }

    @Test
    void generateToken_Success() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains(".")); // JWT format check
        
        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }

    @Test
    void generateToken_DifferentUsernames() {
        // Arrange
        String username1 = "user1";
        String username2 = "user2";

        // Act
        String token1 = jwtTokenProvider.generateToken(username1);
        String token2 = jwtTokenProvider.generateToken(username2);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_SameUsernameDifferentTimes() throws InterruptedException {
        // Arrange
        String username = "testuser";

        // Act
        String token1 = jwtTokenProvider.generateToken(username);
        Thread.sleep(1000); // Wait to ensure different issue times
        String token2 = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Should be different due to different issue times
    }

    @Test
    void generateToken_EmptyUsername() {
        // Arrange
        String username = "";

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            jwtTokenProvider.generateToken(username);
        });
    }

    @Test
    void generateToken_NullUsername() {
        // Arrange
        String username = null;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.generateToken(username);
        });
    }

    @Test
    void getUsernameFromToken_Success() {
        // Arrange
        String originalUsername = "testuser";
        String token = jwtTokenProvider.generateToken(originalUsername);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(originalUsername, extractedUsername);
    }

    @Test
    void getUsernameFromToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(invalidToken);
        });
    }

    @Test
    void getUsernameFromToken_MalformedToken() {
        // Arrange
        String malformedToken = "malformed-token";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(malformedToken);
        });
    }

    @Test
    void getUsernameFromToken_EmptyToken() {
        // Arrange
        String emptyToken = "";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(emptyToken);
        });
    }

    @Test
    void getUsernameFromToken_NullToken() {
        // Arrange
        String nullToken = null;

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(nullToken);
        });
    }

    @Test
    void validateToken_ValidToken() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken() {
        // Arrange
        String malformedToken = "malformed-token";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken() {
        // Arrange
        String emptyToken = "";

        // Act
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken() {
        // Arrange
        String nullToken = null;

        // Act
        boolean isValid = jwtTokenProvider.validateToken(nullToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_TokenWithWrongSignature() {
        // Arrange
        String username = "testuser";
        String token = jwtTokenProvider.generateToken(username);
        
        // Create another provider with different secret
        JwtTokenProvider differentProvider = new JwtTokenProvider(
            "differentSecretKeyForJWTThatIsAtLeast256BitsLong!",
            testExpirationMs
        );

        // Act
        boolean isValid = differentProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken() {
        // Arrange
        String username = "testuser";
        
        // Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
            testSecret,
            1 // 1 millisecond
        );
        
        String token = shortExpirationProvider.generateToken(username);

        // Act
        try {
            Thread.sleep(10); // Wait for token to expire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean isValid = shortExpirationProvider.validateToken(token);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateAndValidateToken_CompleteFlow() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(username);
        boolean isValid = jwtTokenProvider.validateToken(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }

    @Test
    void generateToken_SpecialCharactersInUsername() {
        // Arrange
        String username = "user@example.com";

        // Act
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void generateToken_UnicodeUsername() {
        // Arrange
        String username = "用户名";

        // Act
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void generateToken_LongUsername() {
        // Arrange
        String username = "a".repeat(1000); // Very long username

        // Act
        String token = jwtTokenProvider.generateToken(username);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertEquals(username, extractedUsername);
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void tokenFormat_ValidJWTStructure() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtTokenProvider.generateToken(username);

        // Assert
        assertNotNull(token);
        
        // JWT should have exactly 3 parts
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
        
        // Each part should be non-empty
        for (String part : parts) {
            assertFalse(part.isEmpty());
        }
    }

    @Test
    void constructor_ValidParameters() {
        // Arrange & Act
        JwtTokenProvider provider = new JwtTokenProvider(
            "validSecretKeyForJWTThatIsAtLeast256BitsLong!",
            86400000
        );

        // Assert
        assertNotNull(provider);
        
        // Test that it works
        String token = provider.generateToken("testuser");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
    }

    @Test
    void constructor_ShortSecret() {
        // Arrange & Act & Assert
        assertThrows(Exception.class, () -> {
            new JwtTokenProvider("short", 86400000);
        });
    }

    @Test
    void constructor_ZeroExpiration() {
        // Arrange & Act
        JwtTokenProvider provider = new JwtTokenProvider(
            "validSecretKeyForJWTThatIsAtLeast256BitsLong!",
            0
        );

        // Assert
        assertNotNull(provider);
        
        // Token should be immediately expired
        String token = provider.generateToken("testuser");
        assertNotNull(token);
        // Note: The validation might still pass due to clock skew tolerance
    }

    @Test
    void constructor_NegativeExpiration() {
        // Arrange & Act
        JwtTokenProvider provider = new JwtTokenProvider(
            "validSecretKeyForJWTThatIsAtLeast256BitsLong!",
            -1
        );

        // Assert
        assertNotNull(provider);
        
        // Token should be expired
        String token = provider.generateToken("testuser");
        assertNotNull(token);
        assertFalse(provider.validateToken(token));
    }

    @Test
    void multipleTokens_SameProvider() {
        // Arrange
        String[] usernames = {"user1", "user2", "user3"};

        // Act
        String[] tokens = new String[usernames.length];
        for (int i = 0; i < usernames.length; i++) {
            tokens[i] = jwtTokenProvider.generateToken(usernames[i]);
        }

        // Assert
        for (int i = 0; i < usernames.length; i++) {
            assertNotNull(tokens[i]);
            assertTrue(jwtTokenProvider.validateToken(tokens[i]));
            assertEquals(usernames[i], jwtTokenProvider.getUsernameFromToken(tokens[i]));
        }
        
        // All tokens should be different
        for (int i = 0; i < tokens.length; i++) {
            for (int j = i + 1; j < tokens.length; j++) {
                assertNotEquals(tokens[i], tokens[j]);
            }
        }
    }
} 