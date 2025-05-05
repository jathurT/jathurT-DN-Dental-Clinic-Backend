package com.uor.eng.security;

import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.security.jwt.AuthEntryPointJwt;
import com.uor.eng.security.jwt.AuthTokenFilter;
import com.uor.eng.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

  @Mock
  private UserDetailsServiceImpl userDetailsService;

  @Mock
  private AuthEntryPointJwt unauthorizedHandler;

  @Mock
  private CorsConfigurationSource corsConfigurationSource;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private HttpSecurity httpSecurity;

  @Mock
  private AuthenticationConfiguration authenticationConfiguration;

  private WebSecurityConfig webSecurityConfig;

  @BeforeEach
  void setUp() {
    webSecurityConfig = new WebSecurityConfig(
            userDetailsService,
            unauthorizedHandler,
            corsConfigurationSource
    );

    // Set default values for required properties
    ReflectionTestUtils.setField(webSecurityConfig, "adminUsername", "admin");
    ReflectionTestUtils.setField(webSecurityConfig, "adminEmail", "admin@example.com");
    ReflectionTestUtils.setField(webSecurityConfig, "adminPassword", "adminPass");
    ReflectionTestUtils.setField(webSecurityConfig, "doctorUsername", "doctor1");
    ReflectionTestUtils.setField(webSecurityConfig, "doctorEmail", "doctor1@example.com");
    ReflectionTestUtils.setField(webSecurityConfig, "doctorPassword", "password1");
    ReflectionTestUtils.setField(webSecurityConfig, "receptionistUsername", "receptionist1");
    ReflectionTestUtils.setField(webSecurityConfig, "receptionistEmail", "receptionist1@example.com");
    ReflectionTestUtils.setField(webSecurityConfig, "receptionistPassword", "password2");
  }

  @Test
  void testAuthenticationJwtTokenFilter() {
    AuthTokenFilter filter = webSecurityConfig.authenticationJwtTokenFilter();
    assertNotNull(filter);
  }

  @Test
  void testPasswordEncoder() {
    PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
    assertNotNull(encoder);
    assertInstanceOf(BCryptPasswordEncoder.class, encoder);

    // Verify it works
    String password = "password";
    String encoded = encoder.encode(password);
    assertNotEquals(password, encoded);
    assertTrue(encoder.matches(password, encoded));
  }

  @Test
  void testAuthenticationProvider() {
    DaoAuthenticationProvider provider = webSecurityConfig.authenticationProvider();
    assertNotNull(provider);
  }

  @Test
  void testAuthenticationManager() throws Exception {
    AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
    when(authenticationConfiguration.getAuthenticationManager()).thenReturn(mockAuthManager);

    AuthenticationManager result = webSecurityConfig.authenticationManager(authenticationConfiguration);
    assertNotNull(result);
    assertEquals(mockAuthManager, result);
  }

  @Test
  void testWebSecurityCustomizer() {
    assertNotNull(webSecurityConfig.webSecurityCustomizer());
  }

  @Test
  void testInitData() throws Exception {
    // Mock role repository behavior
    Role dentistRole = new Role(AppRole.ROLE_DENTIST);
    Role receptionistRole = new Role(AppRole.ROLE_RECEPTIONIST);
    Role adminRole = new Role(AppRole.ROLE_ADMIN);

    when(roleRepository.findByRoleName(AppRole.ROLE_DENTIST)).thenReturn(Optional.of(dentistRole));
    when(roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST)).thenReturn(Optional.of(receptionistRole));
    when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

    // Mock user repository behavior
    when(userRepository.existsByUserName("doctor1")).thenReturn(false);
    when(userRepository.existsByUserName("receptionist1")).thenReturn(true);
    when(userRepository.existsByUserName("admin")).thenReturn(true);

    User doctorUser = new User("doctor1", "doctor1@example.com", "encodedPassword");
    User receptionistUser = new User("receptionist1", "receptionist1@example.com", "encodedPassword");
    User adminUser = new User("admin", "admin@example.com", "encodedPassword");

    // Add stubs for findByUserName
    when(userRepository.findByUserName("doctor1")).thenReturn(Optional.of(doctorUser));
    when(userRepository.findByUserName("receptionist1")).thenReturn(Optional.of(receptionistUser));
    when(userRepository.findByUserName("admin")).thenReturn(Optional.of(adminUser));

    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    // Get the CommandLineRunner
    CommandLineRunner runner = webSecurityConfig.initData(roleRepository, userRepository, passwordEncoder);
    assertNotNull(runner);

    // Execute the runner
    runner.run();

    // Update verification to expect 4 calls instead of 3
    verify(userRepository, times(4)).save(any(User.class));

    // Verify doctor user was created with correct username
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository, atLeastOnce()).save(userCaptor.capture());

    boolean doctorFound = userCaptor.getAllValues().stream()
            .anyMatch(user -> "doctor1".equals(user.getUserName()));
    assertTrue(doctorFound, "Doctor user should have been created");
  }
}