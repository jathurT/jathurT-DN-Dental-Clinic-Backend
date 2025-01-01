package com.uor.eng.service.impl;

import com.uor.eng.exceptions.APIException;
import com.uor.eng.model.PasswordResetToken;
import com.uor.eng.model.User;
import com.uor.eng.payload.ForgotPasswordRequest;
import com.uor.eng.payload.ResetPasswordRequest;
import com.uor.eng.repository.PasswordResetTokenRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.service.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

  private static final int EXPIRATION_MINUTES = 30;

  @Autowired
  private PasswordResetTokenRepository tokenRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JavaMailSender mailSender;

  @Override
  @Transactional
  public void initiatePasswordReset(ForgotPasswordRequest request) {
    log.info("Initiating password reset for user with email: {}", request.getEmail());

    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new APIException("User with the given email does not exist"));
    tokenRepository.deleteByUser(user);
    String token = UUID.randomUUID().toString();
    PasswordResetToken passwordResetToken = new PasswordResetToken(
        token,
        user,
        LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)
    );

    tokenRepository.save(passwordResetToken);
    sendPasswordResetEmail(user.getEmail(), token);
  }

  public void sendPasswordResetEmail(String toEmail, String token) {
    log.info("Sending password reset email to: {}", toEmail);

    String resetLink = "https://localhost:5173/reset-password?token=" + token;

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
