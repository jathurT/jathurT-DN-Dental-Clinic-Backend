package com.uor.eng.security.services;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import com.uor.eng.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Create roles
    Set<Role> roles = new HashSet<>();
    Role role = new Role();
    role.setRoleId(1L);
    role.setRoleName(AppRole.ROLE_DENTIST);
    roles.add(role);

    // Create user
    testUser = new User();
    testUser.setUserId(1L);
    testUser.setUserName("testUser");
    testUser.setEmail("test@example.com");
    testUser.setPassword("password123");
    testUser.setRoles(roles);
  }

  @Test
  void loadUserByUsername_WhenFoundByUsername_ShouldReturnUserDetails() {
    // Arrange
    when(userRepository.findByUserName("testUser")).thenReturn(Optional.of(testUser));

    // Act
    UserDetails result = userDetailsService.loadUserByUsername("testUser");

    // Assert
    assertNotNull(result);
    assertEquals("testUser", result.getUsername());
    assertInstanceOf(UserDetailsImpl.class, result);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;
    assertEquals(testUser.getUserId(), userDetails.getId());
    assertEquals(testUser.getEmail(), userDetails.getEmail());
    assertEquals(testUser.getPassword(), userDetails.getPassword());
    assertEquals(1, userDetails.getAuthorities().size());
  }

  @Test
  void loadUserByUsername_WhenFoundByEmail_ShouldReturnUserDetails() {
    // Arrange
    when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

    // Act
    UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

    // Assert
    assertNotNull(result);
    assertEquals("testUser", result.getUsername());
    assertInstanceOf(UserDetailsImpl.class, result);
    UserDetailsImpl userDetails = (UserDetailsImpl) result;
    assertEquals(testUser.getUserId(), userDetails.getId());
    assertEquals(testUser.getEmail(), userDetails.getEmail());
  }

  @Test
  void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
    // Arrange
    when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("nonexistent"));

    String expectedMessage = "User not found with username or email: nonexistent";
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void testConstructor() {
    // Test constructor initializes repository correctly
    UserDetailsServiceImpl service = new UserDetailsServiceImpl(userRepository);
    assertNotNull(service);

    // Verify it works by mocking repository behavior and calling the service
    when(userRepository.findByUserName("testUser")).thenReturn(Optional.of(testUser));
    UserDetails result = service.loadUserByUsername("testUser");
    assertNotNull(result);
  }
}