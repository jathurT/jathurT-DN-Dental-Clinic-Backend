package com.uor.eng.service.impl;

import com.uor.eng.exceptions.APIException;
import com.uor.eng.model.PasswordResetToken;
import com.uor.eng.model.User;
import com.uor.eng.payload.auth.ForgotPasswordRequest;
import com.uor.eng.payload.auth.ResetPasswordRequest;
import com.uor.eng.repository.PasswordResetTokenRepository;
import com.uor.eng.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceImplTest {

  @Mock
  private PasswordResetTokenRepository tokenRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private MimeMessage mimeMessage;

  @Mock
  private ClassPathResource mockResource;

  @InjectMocks
  private PasswordResetServiceImpl passwordResetService;

  private User testUser;
  private PasswordResetToken testToken;
  private String testEmail = "test@example.com";
  private String testTokenValue = UUID.randomUUID().toString();

  @BeforeEach
  void setUp() {
    testUser = new User();
    testUser.setUserId(1L);
    testUser.setUserName("testuser");
    testUser.setEmail(testEmail);
    testUser.setPassword("oldPassword");

    testToken = new PasswordResetToken(testTokenValue, testUser, LocalDateTime.now().plusMinutes(30));

    ReflectionTestUtils.setField(passwordResetService, "resetPasswordLink", "http://localhost:3000/reset-password/");
  }

  @Test
  void initiatePasswordReset_Success() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail(testEmail);

    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    when(tokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

    // Mock the sendPasswordResetEmail method
    doNothing().when(mailSender).send(any(MimeMessage.class));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    try (MockedStatic<IOUtils> ioUtilsMocked = mockStatic(IOUtils.class)) {
      // Mock ClassPathResource and IOUtils behavior
      ioUtilsMocked
              .when(() -> IOUtils.toString(any(InputStream.class), eq(StandardCharsets.UTF_8)))
              .thenReturn("Template with {{resetLink}} and {{expirationMinutes}} and {{currentYear}}");

      // When
      passwordResetService.initiatePasswordReset(request);

      // Then
      verify(userRepository).findByEmail(testEmail);
      verify(tokenRepository).findByUser(testUser);
      verify(tokenRepository).save(any(PasswordResetToken.class));
    }
  }

  @Test
  void initiatePasswordReset_UpdateExistingToken() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail(testEmail);

    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    when(tokenRepository.findByUser(testUser)).thenReturn(Optional.of(testToken));
    when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(testToken);

    // Mock the sendPasswordResetEmail method
    doNothing().when(mailSender).send(any(MimeMessage.class));
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    try (MockedStatic<IOUtils> ioUtilsMocked = mockStatic(IOUtils.class)) {
      // Mock ClassPathResource and IOUtils behavior
      ioUtilsMocked
              .when(() -> IOUtils.toString(any(InputStream.class), eq(StandardCharsets.UTF_8)))
              .thenReturn("Template with {{resetLink}} and {{expirationMinutes}} and {{currentYear}}");

      // When
      passwordResetService.initiatePasswordReset(request);

      // Then
      verify(userRepository).findByEmail(testEmail);
      verify(tokenRepository).findByUser(testUser);
      verify(tokenRepository).save(any(PasswordResetToken.class));
    }
  }

  @Test
  void initiatePasswordReset_UserNotFound() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("nonexistent@example.com");

    when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

    // When & Then
    APIException exception = assertThrows(APIException.class,
            () -> passwordResetService.initiatePasswordReset(request));
    assertEquals("User with the given email does not exist", exception.getMessage());
    verify(userRepository).findByEmail("nonexistent@example.com");
    verify(tokenRepository, never()).save(any(PasswordResetToken.class));
  }

  @Test
  void initiatePasswordReset_DataIntegrityViolation() {
    // Given
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail(testEmail);

    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
    when(tokenRepository.findByUser(testUser)).thenReturn(Optional.empty());
    when(tokenRepository.save(any(PasswordResetToken.class))).thenThrow(DataIntegrityViolationException.class);

    // When & Then
    APIException exception = assertThrows(APIException.class,
            () -> passwordResetService.initiatePasswordReset(request));
    assertEquals("A password reset is already in progress. Please check your email.", exception.getMessage());
    verify(userRepository).findByEmail(testEmail);
    verify(tokenRepository).findByUser(testUser);
    verify(tokenRepository).save(any(PasswordResetToken.class));
    verify(mailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  void sendPasswordResetEmail_Success() {
    // Given
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    doNothing().when(mailSender).send(any(MimeMessage.class));

    try (MockedStatic<IOUtils> ioUtilsMocked = mockStatic(IOUtils.class)) {
      // Mock ClassPathResource behavior
      ioUtilsMocked
              .when(() -> IOUtils.toString(any(InputStream.class), eq(StandardCharsets.UTF_8)))
              .thenReturn("Template with {{resetLink}} and {{expirationMinutes}} and {{currentYear}}");

      // When
      passwordResetService.sendPasswordResetEmail(testEmail, testTokenValue);

      // Then
      verify(mailSender).createMimeMessage();
      verify(mailSender).send(any(MimeMessage.class));
    }
  }

  @Test
  void resetPassword_Success() {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken(testTokenValue);
    request.setNewPassword("newPassword123");

    when(tokenRepository.findByToken(testTokenValue)).thenReturn(Optional.of(testToken));
    when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");

    // When
    passwordResetService.resetPassword(request);

    // Then
    verify(tokenRepository).findByToken(testTokenValue);
    verify(passwordEncoder).encode("newPassword123");
    verify(userRepository).save(testUser);
    verify(tokenRepository).delete(testToken);
    assertEquals("encodedNewPassword", testUser.getPassword());
  }

  @Test
  void resetPassword_InvalidToken() {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken("invalidToken");
    request.setNewPassword("newPassword123");

    when(tokenRepository.findByToken("invalidToken")).thenReturn(Optional.empty());

    // When & Then
    APIException exception = assertThrows(APIException.class,
            () -> passwordResetService.resetPassword(request));
    assertEquals("Invalid password reset token", exception.getMessage());
    verify(tokenRepository).findByToken("invalidToken");
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void resetPassword_ExpiredToken() {
    // Given
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setToken(testTokenValue);
    request.setNewPassword("newPassword123");

    PasswordResetToken expiredToken = new PasswordResetToken(testTokenValue, testUser, LocalDateTime.now().minusMinutes(5));
    when(tokenRepository.findByToken(testTokenValue)).thenReturn(Optional.of(expiredToken));

    // When & Then
    APIException exception = assertThrows(APIException.class,
            () -> passwordResetService.resetPassword(request));
    assertEquals("Password reset token has expired", exception.getMessage());
    verify(tokenRepository).findByToken(testTokenValue);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(User.class));
  }
}