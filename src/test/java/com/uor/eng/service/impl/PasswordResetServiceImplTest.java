package com.uor.eng.service.impl;

import com.uor.eng.exceptions.APIException;
import com.uor.eng.exceptions.EmailSendingException;
import com.uor.eng.model.PasswordResetToken;
import com.uor.eng.model.User;
import com.uor.eng.payload.auth.ForgotPasswordRequest;
import com.uor.eng.payload.auth.ResetPasswordRequest;
import com.uor.eng.repository.PasswordResetTokenRepository;
import com.uor.eng.repository.UserRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PasswordResetServiceImplTest {

  @InjectMocks
  private PasswordResetServiceImpl passwordResetService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordResetTokenRepository tokenRepository;

  @Mock
  private JavaMailSender mailSender;

  @Mock
  private PasswordEncoder passwordEncoder;

  private User user;
  private PasswordResetToken passwordResetToken;

  @BeforeEach
  void setUp() {
    user = new User();
    user.setEmail("john@example.com");
    user.setPassword("oldPassword");

    passwordResetToken = new PasswordResetToken("validToken", user, LocalDateTime.now().plusMinutes(30));

    MimeMessage mimeMessage = mock(MimeMessage.class);
    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
  }

  @AfterEach
  void tearDown() {
    reset(userRepository, tokenRepository, mailSender, passwordEncoder);
  }

  @Test
  @DisplayName("Test initiate password reset - Success")
  @Order(1)
  void initiatePasswordReset_ShouldGenerateTokenAndSendEmail_WhenUserExists() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());
    doNothing().when(mailSender).send(any(MimeMessage.class));

    assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset(request));
    verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("Test initiate password reset - User not found")
  @Order(2)
  void initiatePasswordReset_ShouldThrowAPIException_WhenUserDoesNotExist() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThrows(APIException.class, () -> passwordResetService.initiatePasswordReset(request));
    verify(tokenRepository, never()).save(any());
    verify(mailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("Test initiate password reset - Existing token updated")
  @Order(3)
  void initiatePasswordReset_ShouldUpdateToken_WhenUserAlreadyHasOne() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(tokenRepository.findByUser(user)).thenReturn(Optional.of(passwordResetToken));

    assertDoesNotThrow(() -> passwordResetService.initiatePasswordReset(request));
    verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    verify(mailSender, times(1)).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("Test initiate password reset - Token saving fails")
  @Order(4)
  void initiatePasswordReset_ShouldThrowAPIException_WhenTokenSaveFails() {
    ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(tokenRepository.findByUser(user)).thenReturn(Optional.empty());
    doThrow(new DataIntegrityViolationException("Duplicate entry")).when(tokenRepository).save(any(PasswordResetToken.class));

    APIException exception = assertThrows(APIException.class, () -> passwordResetService.initiatePasswordReset(request));
    assertEquals("A password reset is already in progress. Please check your email.", exception.getMessage());

    verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
    verify(mailSender, never()).send(any(MimeMessage.class));
  }

  @Test
  @DisplayName("Test send password reset email - MailException occurs")
  @Order(5)
  void sendPasswordResetEmail_ShouldThrowEmailSendingException_WhenMailFails() {
    when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));
    doThrow(new EmailSendingException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

    assertThrows(EmailSendingException.class, () -> passwordResetService.sendPasswordResetEmail("john@example.com", "testToken"));
  }


  @Test
  @DisplayName("Test reset password - Success")
  @Order(6)
  void resetPassword_ShouldUpdatePasswordAndDeleteToken_WhenValidTokenProvided() {
    ResetPasswordRequest request = new ResetPasswordRequest("validToken", "newPassword123");
    when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(passwordResetToken));
    when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedPassword");

    assertDoesNotThrow(() -> passwordResetService.resetPassword(request));
    verify(userRepository, times(1)).save(any(User.class));
    verify(tokenRepository, times(1)).delete(passwordResetToken);
  }

  @Test
  @DisplayName("Test reset password - Expired Token")
  @Order(7)
  void resetPassword_ShouldThrowAPIException_WhenTokenExpired() {
    ResetPasswordRequest request = new ResetPasswordRequest("expiredToken", "newPassword123");
    passwordResetToken.setExpiryDate(LocalDateTime.now().minusMinutes(1));
    when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.of(passwordResetToken));

    assertThrows(APIException.class, () -> passwordResetService.resetPassword(request));
    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).delete(any());
  }

  @Test
  @DisplayName("Test reset password - Token not found")
  @Order(8)
  void resetPassword_ShouldThrowAPIException_WhenTokenNotFound() {
    ResetPasswordRequest request = new ResetPasswordRequest("invalidToken", "newPassword");
    when(tokenRepository.findByToken(request.getToken())).thenReturn(Optional.empty());

    APIException exception = assertThrows(APIException.class, () -> passwordResetService.resetPassword(request));
    assertEquals("Invalid password reset token", exception.getMessage());

    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).delete(any());
  }
}
