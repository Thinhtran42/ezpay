package com.thinhtran.EzPay.repository;

import com.thinhtran.EzPay.entity.Role;
import com.thinhtran.EzPay.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        testUser1 = User.builder()
                .userName("testuser1")
                .email("test1@example.com")
                .password("encodedPassword1")
                .fullName("Test User 1")
                .role(Role.USER)
                .balance(1000.0)
                .phone("123456789")
                .build();

        testUser2 = User.builder()
                .userName("testuser2")
                .email("test2@example.com")
                .password("encodedPassword2")
                .fullName("Test User 2")
                .role(Role.ADMIN)
                .balance(2000.0)
                .phone("987654321")
                .build();
    }

    @Test
    void findByUserName_Success() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> result = userRepository.findByUserName("testuser1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser1", result.get().getUserName());
        assertEquals("test1@example.com", result.get().getEmail());
        assertEquals("Test User 1", result.get().getFullName());
        assertEquals(Role.USER, result.get().getRole());
        assertEquals(1000.0, result.get().getBalance());
        assertEquals("123456789", result.get().getPhone());
    }

    @Test
    void findByUserName_NotFound() {
        // Act
        Optional<User> result = userRepository.findByUserName("nonexistent");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByUserName_CaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> result = userRepository.findByUserName("TestUser1");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void existsByUserName_True() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByUserName("testuser1");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByUserName_False() {
        // Act
        boolean exists = userRepository.existsByUserName("nonexistent");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByUserName_CaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByUserName("TestUser1");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> result = userRepository.findByEmail("test1@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser1", result.get().getUserName());
        assertEquals("test1@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_NotFound() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_CaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> result = userRepository.findByEmail("Test1@Example.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void existsByEmail_True() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByEmail("test1@example.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_False() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void existsByEmail_CaseSensitive() {
        // Arrange
        entityManager.persistAndFlush(testUser1);

        // Act
        boolean exists = userRepository.existsByEmail("Test1@Example.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void save_Success() {
        // Act
        User savedUser = userRepository.save(testUser1);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("testuser1", savedUser.getUserName());
        assertEquals("test1@example.com", savedUser.getEmail());
        assertEquals("Test User 1", savedUser.getFullName());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(1000.0, savedUser.getBalance());
        assertEquals("123456789", savedUser.getPhone());
    }

    @Test
    void save_WithNullValues() {
        // Arrange
        User userWithNulls = User.builder()
                .userName("nulluser")
                .email("null@example.com")
                .password("password")
                .fullName(null)
                .role(Role.USER)
                .balance(0.0)
                .phone(null)
                .build();

        // Act
        User savedUser = userRepository.save(userWithNulls);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("nulluser", savedUser.getUserName());
        assertEquals("null@example.com", savedUser.getEmail());
        assertNull(savedUser.getFullName());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(0.0, savedUser.getBalance());
        assertNull(savedUser.getPhone());
    }

    @Test
    void save_WithDefaultValues() {
        // Arrange
        User userWithDefaults = User.builder()
                .userName("defaultuser")
                .email("default@example.com")
                .password("password")
                .fullName("Default User")
                .build();

        // Act
        User savedUser = userRepository.save(userWithDefaults);

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("defaultuser", savedUser.getUserName());
        assertEquals("default@example.com", savedUser.getEmail());
        assertEquals("Default User", savedUser.getFullName());
        assertEquals(Role.USER, savedUser.getRole()); // Default role
        assertEquals(0.0, savedUser.getBalance()); // Default balance
    }

    @Test
    void update_UserBalance() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        Long userId = savedUser.getId();

        // Act
        savedUser.setBalance(1500.0);
        User updatedUser = userRepository.save(savedUser);

        // Assert
        assertEquals(userId, updatedUser.getId());
        assertEquals(1500.0, updatedUser.getBalance());
    }

    @Test
    void findById_Success() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);

        // Act
        Optional<User> result = userRepository.findById(savedUser.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedUser.getId(), result.get().getId());
        assertEquals("testuser1", result.get().getUserName());
    }

    @Test
    void findById_NotFound() {
        // Act
        Optional<User> result = userRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void deleteById_Success() {
        // Arrange
        User savedUser = entityManager.persistAndFlush(testUser1);
        Long userId = savedUser.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<User> result = userRepository.findById(userId);
        assertFalse(result.isPresent());
    }

    @Test
    void findAll_Success() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Act
        var users = userRepository.findAll();

        // Assert
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUserName().equals("testuser1")));
        assertTrue(users.stream().anyMatch(u -> u.getUserName().equals("testuser2")));
    }

    @Test
    void findAll_Empty() {
        // Act
        var users = userRepository.findAll();

        // Assert
        assertTrue(users.isEmpty());
    }

    @Test
    void count_Success() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);

        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(2, count);
    }

    @Test
    void count_Empty() {
        // Act
        long count = userRepository.count();

        // Assert
        assertEquals(0, count);
    }

    @Test
    void uniqueConstraints_Username() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        User duplicateUser = User.builder()
                .userName("testuser1") // Same username
                .email("different@example.com")
                .password("password")
                .fullName("Different User")
                .role(Role.USER)
                .balance(0.0)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        });
    }

    @Test
    void uniqueConstraints_Email() {
        // Arrange
        entityManager.persistAndFlush(testUser1);
        
        User duplicateUser = User.builder()
                .userName("differentuser")
                .email("test1@example.com") // Same email
                .password("password")
                .fullName("Different User")
                .role(Role.USER)
                .balance(0.0)
                .build();

        // Act & Assert
        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateUser);
            entityManager.flush();
        });
    }

    @Test
    void nullableFields_Phone() {
        // Arrange
        User userWithoutPhone = User.builder()
                .userName("nophoneuser")
                .email("nophone@example.com")
                .password("password")
                .fullName("No Phone User")
                .role(Role.USER)
                .balance(0.0)
                .phone(null)
                .build();

        // Act
        User savedUser = userRepository.save(userWithoutPhone);

        // Assert
        assertNotNull(savedUser.getId());
        assertNull(savedUser.getPhone());
    }

    @Test
    void roleEnum_Values() {
        // Arrange
        testUser1.setRole(Role.USER);
        testUser2.setRole(Role.ADMIN);

        // Act
        User savedUser1 = entityManager.persistAndFlush(testUser1);
        User savedUser2 = entityManager.persistAndFlush(testUser2);

        // Assert
        assertEquals(Role.USER, savedUser1.getRole());
        assertEquals(Role.ADMIN, savedUser2.getRole());
    }
} 