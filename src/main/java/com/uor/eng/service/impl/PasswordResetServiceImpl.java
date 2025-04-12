package com.uor.eng.service.impl;

import com.uor.eng.exceptions.APIException;
import com.uor.eng.exceptions.EmailSendingException;
import com.uor.eng.model.PasswordResetToken;
import com.uor.eng.model.User;
import com.uor.eng.payload.auth.ForgotPasswordRequest;
import com.uor.eng.payload.auth.ResetPasswordRequest;
import com.uor.eng.repository.PasswordResetTokenRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

  private static final int EXPIRATION_MINUTES = 30;

  @Value("${app.reset-password-link}")
  private String resetPasswordLink;

  private final PasswordResetTokenRepository tokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JavaMailSender mailSender;

  public PasswordResetServiceImpl(PasswordResetTokenRepository tokenRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  JavaMailSender mailSender) {
    this.tokenRepository = tokenRepository;
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.mailSender = mailSender;
  }

  @Override
  @Transactional
  public void initiatePasswordReset(ForgotPasswordRequest request) {
    log.info("Initiating password reset for user with email: {}", request.getEmail());

    User user;
    try {
      user = userRepository.findByEmail(request.getEmail())
              .orElseThrow(() -> new APIException("User with the given email does not exist"));
    } catch (APIException e) {
      log.warn("Password reset requested for non-existent email: {}", request.getEmail());
      throw new APIException("User with the given email does not exist");
    } catch (Exception e) {
      log.error("Unexpected error while retrieving user with email: {}", request.getEmail(), e);
      throw new APIException("An unexpected error occurred. Please try again later.");
    }

    String token = UUID.randomUUID().toString();
    LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);

    PasswordResetToken passwordResetToken;
    try {
      Optional<PasswordResetToken> optionalToken = tokenRepository.findByUser(user);

      if (optionalToken.isPresent()) {
        passwordResetToken = optionalToken.get();
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expirationTime);
        log.info("Updated existing password reset token for user: {}", user.getEmail());
      } else {
        passwordResetToken = new PasswordResetToken(token, user, expirationTime);
        log.info("Created new password reset token for user: {}", user.getEmail());
      }

      tokenRepository.save(passwordResetToken);
    } catch (DataIntegrityViolationException e) {
      log.error("Data integrity violation while saving token for user: {}", user.getEmail(), e);
      throw new APIException("A password reset is already in progress. Please check your email.");
    } catch (Exception e) {
      log.error("Unexpected error while saving token for user: {}", user.getEmail(), e);
      throw new APIException("Unable to process password reset. Please try again later.");
    }

    try {
      sendPasswordResetEmail(user.getEmail(), token);
      log.info("Password reset email sent to user: {}", user.getEmail());
    } catch (MailException e) {
      log.error("Error sending password reset email to user: {}", user.getEmail(), e);
      throw new EmailSendingException("Failed to send password reset email. Please try again later.");
    } catch (Exception e) {
      log.error("Unexpected error while sending password reset email to user: {}", user.getEmail(), e);
      throw new APIException("An unexpected error occurred while sending the email. Please try again later.");
    }
  }


  public void sendPasswordResetEmail(String toEmail, String token) {
    log.info("Sending password reset email to: {}", toEmail);

    String resetLink = resetPasswordLink + token;

    try {
      ClassPathResource templateResource = new ClassPathResource("templates/passwordReset.html");
      String htmlTemplate = IOUtils.toString(templateResource.getInputStream(), StandardCharsets.UTF_8);

      String htmlContent = htmlTemplate
              .replace("{{resetLink}}", resetLink)
              .replace("{{expirationMinutes}}", String.valueOf(EXPIRATION_MINUTES))
              .replace("{{currentYear}}", String.valueOf(LocalDate.now().getYear()));

      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
      helper.setTo(toEmail);
      helper.setSubject("Password Reset Request");
      helper.setText(htmlContent, true);

      mailSender.send(mimeMessage);
      log.info("Password reset email sent successfully to: {}", toEmail);
    } catch (IOException e) {
      log.error("Failed to load email template", e);
      throw new APIException("Failed to send password reset email");
    } catch (MessagingException e) {
      log.error("Failed to send password reset email to: {}", toEmail, e);
      throw new APIException("Failed to send password reset email");
    }
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    log.info("Resetting password for user with token: {}", request.getToken());
    PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
            .orElseThrow(() -> new APIException("Invalid password reset token"));

    if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
      throw new APIException("Password reset token has expired");
    }

    User user = resetToken.getUser();
    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
    tokenRepository.delete(resetToken);
  }
}
