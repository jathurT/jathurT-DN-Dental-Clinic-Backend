package com.uor.eng.repository;

import com.uor.eng.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .userName("testuser")
        .email("testuser@example.com")
        .password("password123")
        .build();
    userRepository.save(testUser);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find by user name")
  void testFindByUserName_ShouldReturnUser() {
    Optional<User> foundUser = userRepository.findByUserName("testuser");
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getUserName()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Test find by user name with existent user")
  void testExistsByUserName_ShouldReturnTrue() {
    Boolean exists = userRepository.existsByUserName("testuser");
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Test find by user name with non-existent user")
  void testExistsByUserName_ShouldReturnFalse() {
    Boolean exists = userRepository.existsByUserName("nonexistentuser");
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Test exists by email with existing user")
  void testExistsByEmail_ShouldReturnTrue() {
    Boolean exists = userRepository.existsByEmail("testuser@example.com");
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("Test exists by email with non-existent user")
  void testExistsByEmail_ShouldReturnFalse() {
    Boolean exists = userRepository.existsByEmail("nonexistent@example.com");
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("Test find by email with existing user")
  void testFindByEmail_ShouldReturnUser() {
    Optional<User> foundUser = userRepository.findByEmail("testuser@example.com");
    assertThat(foundUser).isPresent();
    assertThat(foundUser.get().getEmail()).isEqualTo("testuser@example.com");
  }

  @Test
  @DisplayName("Test find by email with non-existent user")
  void testFindByEmail_ShouldReturnEmptyOptional() {
    Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
    assertThat(foundUser).isNotPresent();
  }

  @Test
  @DisplayName("Test save user")
  void testSave_ShouldPersistUser() {
    User newUser = User.builder()
        .userName("newuser")
        .email("newuser@example.com")
        .password("password456")
        .build();

    User savedUser = userRepository.save(newUser);

    assertThat(savedUser).isNotNull();
    assertThat(savedUser.getUserId()).isNotNull();
    assertThat(savedUser.getUserName()).isEqualTo("newuser");
    assertThat(savedUser.getEmail()).isEqualTo("newuser@example.com");
  }

  @Test
  @DisplayName("Test delete user")
  void testDelete_ShouldRemoveUser() {
    userRepository.delete(testUser);
    Optional<User> deletedUser = userRepository.findByUserName("testuser");
    assertThat(deletedUser).isNotPresent();
  }
}
