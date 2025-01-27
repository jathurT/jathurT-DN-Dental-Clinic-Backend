package com.uor.eng.repository;

import com.uor.eng.model.PasswordResetToken;
import com.uor.eng.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PasswordResetTokenRepositoryTest {

  @Autowired
  private PasswordResetTokenRepository passwordResetTokenRepository;

  @Autowired
  private UserRepository userRepository;

  private User testUser;
  private PasswordResetToken testToken;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .userName("testuser")
        .email("testuser@example.com")
        .password("password123")
        .build();
    testUser = userRepository.save(testUser);

    testToken = new PasswordResetToken("testToken123", testUser, LocalDateTime.now().plusHours(1));
    testToken = passwordResetTokenRepository.save(testToken);
  }

  @AfterEach
  void tearDown() {
    passwordResetTokenRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("Test find by token")
  @Order(1)
  void testFindByToken() {
    var retrievedToken = passwordResetTokenRepository.findByToken("testToken123");

    assertThat(retrievedToken).isPresent();
    assertThat(retrievedToken.get().getToken()).isEqualTo("testToken123");
    assertThat(retrievedToken.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
  }

  @Test
  void testFindByUser() {
    var retrievedToken = passwordResetTokenRepository.findByUser(testUser);

    assertThat(retrievedToken).isPresent();
    assertThat(retrievedToken.get().getUser().getUserId()).isEqualTo(testUser.getUserId());
    assertThat(retrievedToken.get().getToken()).isEqualTo("testToken123");
  }

  @Test
  void testDeleteByUser() {
    passwordResetTokenRepository.deleteByUser(testUser);

    var retrievedToken = passwordResetTokenRepository.findByUser(testUser);
    assertThat(retrievedToken).isNotPresent();
  }
}
