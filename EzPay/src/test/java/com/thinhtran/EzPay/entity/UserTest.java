package com.thinhtran.EzPay.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void userBuilder_Success() {
        // Arrange & Act
        User user = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(Role.USER)
                .balance(1000.0)
                .phone("123456789")
                .build();

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUserName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("Test User", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(1000.0, user.getBalance());
        assertEquals("123456789", user.getPhone());
    }

    @Test
    void userBuilder_WithDefaults() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .build();

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getUserName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("Test User", user.getFullName());
        assertEquals(Role.USER, user.getRole()); // Default role
        assertEquals(0.0, user.getBalance()); // Default balance
        assertNull(user.getPhone()); // Nullable field
    }

    @Test
    void userBuilder_WithNullValues() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName(null)
                .phone(null)
                .build();

        // Assert
        assertNotNull(user);
        assertEquals("testuser", user.getUserName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getPhone());
    }

    @Test
    void userConstructor_NoArgs() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getUserName());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getFullName());
        assertNull(user.getPhone());
        assertEquals(Role.USER, user.getRole()); // Default role
        assertEquals(0.0, user.getBalance()); // Default balance
    }

    @Test
    void userConstructor_AllArgs() {
        // Arrange & Act
        User user = new User(1L, "testuser", "test@example.com", "password", 
                           "123456789", "Test User", Role.ADMIN, 1500.0);

        // Assert
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUserName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("123456789", user.getPhone());
        assertEquals("Test User", user.getFullName());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(1500.0, user.getBalance());
    }

    @Test
    void userSetters_Success() {
        // Arrange
        User user = new User();

        // Act
        user.setId(2L);
        user.setUserName("newuser");
        user.setEmail("new@example.com");
        user.setPassword("newpassword");
        user.setFullName("New User");
        user.setRole(Role.ADMIN);
        user.setBalance(2000.0);
        user.setPhone("987654321");

        // Assert
        assertEquals(2L, user.getId());
        assertEquals("newuser", user.getUserName());
        assertEquals("new@example.com", user.getEmail());
        assertEquals("newpassword", user.getPassword());
        assertEquals("New User", user.getFullName());
        assertEquals(Role.ADMIN, user.getRole());
        assertEquals(2000.0, user.getBalance());
        assertEquals("987654321", user.getPhone());
    }

    @Test
    void userGetters_Success() {
        // Arrange
        User user = User.builder()
                .id(3L)
                .userName("getteruser")
                .email("getter@example.com")
                .password("getterpassword")
                .fullName("Getter User")
                .role(Role.USER)
                .balance(500.0)
                .phone("111222333")
                .build();

        // Act & Assert
        assertEquals(3L, user.getId());
        assertEquals("getteruser", user.getUserName());
        assertEquals("getter@example.com", user.getEmail());
        assertEquals("getterpassword", user.getPassword());
        assertEquals("Getter User", user.getFullName());
        assertEquals(Role.USER, user.getRole());
        assertEquals(500.0, user.getBalance());
        assertEquals("111222333", user.getPhone());
    }

    @Test
    void userEquals_SameId() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .id(1L)
                .userName("user2")
                .email("user2@example.com")
                .password("password2")
                .build();

        // Act & Assert
        assertEquals(user1, user2); // Should be equal if same ID
    }

    @Test
    void userEquals_DifferentId() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .id(2L)
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        // Act & Assert
        assertNotEquals(user1, user2); // Should not be equal if different ID
    }

    @Test
    void userEquals_NullId() {
        // Arrange
        User user1 = User.builder()
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        // Act & Assert
        assertNotEquals(user1, user2); // Should not be equal if both have null ID
    }

    @Test
    void userHashCode_Consistency() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .build();

        // Act
        int hashCode1 = user.hashCode();
        int hashCode2 = user.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void userHashCode_EqualObjects() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .userName("user1")
                .email("user1@example.com")
                .password("password1")
                .build();

        User user2 = User.builder()
                .id(1L)
                .userName("user2")
                .email("user2@example.com")
                .password("password2")
                .build();

        // Act & Assert
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void userToString_NotNull() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("Test User")
                .role(Role.USER)
                .balance(1000.0)
                .phone("123456789")
                .build();

        // Act
        String toString = user.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.isEmpty());
        assertTrue(toString.contains("User"));
        assertTrue(toString.contains("testuser"));
        assertTrue(toString.contains("test@example.com"));
        // Password should not be in toString for security
        assertFalse(toString.contains("password"));
    }

    @Test
    void userRole_DefaultValue() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertEquals(Role.USER, user.getRole());
    }

    @Test
    void userRole_AdminValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("admin")
                .email("admin@example.com")
                .password("password")
                .role(Role.ADMIN)
                .build();

        // Assert
        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void userBalance_DefaultValue() {
        // Arrange & Act
        User user = new User();

        // Assert
        assertEquals(0.0, user.getBalance());
    }

    @Test
    void userBalance_PositiveValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .balance(999.99)
                .build();

        // Assert
        assertEquals(999.99, user.getBalance());
    }

    @Test
    void userBalance_ZeroValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .balance(0.0)
                .build();

        // Assert
        assertEquals(0.0, user.getBalance());
    }

    @Test
    void userBalance_NegativeValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .balance(-100.0)
                .build();

        // Assert
        assertEquals(-100.0, user.getBalance());
    }

    @Test
    void userPhone_NullValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .phone(null)
                .build();

        // Assert
        assertNull(user.getPhone());
    }

    @Test
    void userPhone_EmptyString() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .phone("")
                .build();

        // Assert
        assertEquals("", user.getPhone());
    }

    @Test
    void userFullName_NullValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName(null)
                .build();

        // Assert
        assertNull(user.getFullName());
    }

    @Test
    void userFullName_EmptyString() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("")
                .build();

        // Assert
        assertEquals("", user.getFullName());
    }

    @Test
    void userEmail_SpecialCharacters() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test+special@example.com")
                .password("password")
                .build();

        // Assert
        assertEquals("test+special@example.com", user.getEmail());
    }

    @Test
    void userUserName_SpecialCharacters() {
        // Arrange & Act
        User user = User.builder()
                .userName("test_user-123")
                .email("test@example.com")
                .password("password")
                .build();

        // Assert
        assertEquals("test_user-123", user.getUserName());
    }

    @Test
    void userPassword_SpecialCharacters() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("P@ssw0rd!")
                .build();

        // Assert
        assertEquals("P@ssw0rd!", user.getPassword());
    }

    @Test
    void userFullName_UnicodeCharacters() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .fullName("测试用户")
                .build();

        // Assert
        assertEquals("测试用户", user.getFullName());
    }

    @Test
    void userPhone_InternationalFormat() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .phone("+1-555-123-4567")
                .build();

        // Assert
        assertEquals("+1-555-123-4567", user.getPhone());
    }

    @Test
    void userBalance_LargeValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .balance(999999999.99)
                .build();

        // Assert
        assertEquals(999999999.99, user.getBalance());
    }

    @Test
    void userBalance_SmallDecimalValue() {
        // Arrange & Act
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .balance(0.01)
                .build();

        // Assert
        assertEquals(0.01, user.getBalance());
    }
} 