package com.uor.eng.util;

import com.uor.eng.model.User;
import com.uor.eng.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthUtilTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @InjectMocks
  private AuthUtil authUtil;

  private final String username = "testUser";
  private final String email = "test@example.com";
  private final Long userId = 1L;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getName()).thenReturn(username);
  }

  @Test
  void loggedInEmail_shouldReturnUserEmail() {
    // Arrange
    User user = new User();
    user.setUserName(username);
    user.setEmail(email);

    when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));

    // Act
    String result = authUtil.loggedInEmail();

    // Assert
    assertEquals(email, result);
    verify(userRepository).findByUserName(username);
  }

  @Test
  void loggedInEmail_shouldThrowExceptionWhenUserNotFound() {
    // Arrange
    when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
      authUtil.loggedInEmail();
    });

    assertTrue(exception.getMessage().contains("User Not Found with username: " + username));
    verify(userRepository).findByUserName(username);
  }

  @Test
  void loggedInUserId_shouldReturnUserId() {
    // Arrange
    User user = new User();
    user.setUserName(username);
    user.setUserId(userId);

    when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));

    // Act
    Long result = authUtil.loggedInUserId();

    // Assert
    assertEquals(userId, result);
    verify(userRepository).findByUserName(username);
  }

  @Test
  void loggedInUserId_shouldThrowExceptionWhenUserNotFound() {
    // Arrange
    when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
      authUtil.loggedInUserId();
    });

    assertTrue(exception.getMessage().contains("User Not Found with username: " + username));
    verify(userRepository).findByUserName(username);
  }

  @Test
  void loggedInUser_shouldReturnUser() {
    // Arrange
    User user = new User();
    user.setUserName(username);
    user.setEmail(email);
    user.setUserId(userId);

    when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));

    // Act
    User result = authUtil.loggedInUser();

    // Assert
    assertEquals(username, result.getUserName());
    assertEquals(email, result.getEmail());
    assertEquals(userId, result.getUserId());
    verify(userRepository).findByUserName(username);
  }

  @Test
  void loggedInUser_shouldThrowExceptionWhenUserNotFound() {
    // Arrange
    when(userRepository.findByUserName(username)).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
      authUtil.loggedInUser();
    });

    assertTrue(exception.getMessage().contains("User Not Found with username: " + username));
    verify(userRepository).findByUserName(username);
  }

  @Test
  void shouldWorkWithRealSecurityContext() {
    // Arrange
    User user = new User();
    user.setUserName(username);
    user.setEmail(email);

    when(userRepository.findByUserName(username)).thenReturn(Optional.of(user));

    // Create a real authentication object and set it in the security context
    Authentication realAuth = new UsernamePasswordAuthenticationToken(username, "password");
    SecurityContextHolder.getContext().setAuthentication(realAuth);

    // Act
    String result = authUtil.loggedInEmail();

    // Assert
    assertEquals(email, result);
    verify(userRepository).findByUserName(username);

    // Reset security context
    SecurityContextHolder.clearContext();
  }
}