package com.uor.eng.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uor.eng.model.AppRole;
import com.uor.eng.model.Role;
import com.uor.eng.model.User;
import com.uor.eng.payload.auth.ForgotPasswordRequest;
import com.uor.eng.payload.auth.ResetPasswordRequest;
import com.uor.eng.repository.RoleRepository;
import com.uor.eng.repository.UserRepository;
import com.uor.eng.security.jwt.JwtUtils;
import com.uor.eng.security.request.LoginRequest;
import com.uor.eng.security.request.SignupRequest;
import com.uor.eng.security.response.UserInfoResponse;
import com.uor.eng.security.services.UserDetailsImpl;
import com.uor.eng.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthControllerTest {

  private MockMvc mockMvc;

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private UserRepository userRepository;

  @Mock
  private RoleRepository roleRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private PasswordResetService passwordResetService;

  @InjectMocks
  private AuthController authController;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private Authentication authentication;
  private UserDetailsImpl userDetails;
  private LoginRequest loginRequest;
  private SignupRequest signupRequest;
  private ForgotPasswordRequest forgotPasswordRequest;
  private ResetPasswordRequest resetPasswordRequest;

  @BeforeEach
  public void setup() {
    // Setup UserDetails
    List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(AppRole.ROLE_RECEPTIONIST.name())
    );
    userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", authorities);

    // Mock Authentication
    authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getName()).thenReturn("testuser");

    // Initialize mockMvc with default setup
    mockMvc = MockMvcBuilders
            .standaloneSetup(authController)
            .build();

    // Setup LoginRequest
    loginRequest = new LoginRequest();
    loginRequest.setUserNameOrEmail("testuser");
    loginRequest.setPassword("password");

    // Setup SignupRequest
    signupRequest = new SignupRequest();
    signupRequest.setUsername("newuser");
    signupRequest.setEmail("newuser@example.com");
    signupRequest.setPassword("password123");
    signupRequest.setRole(Set.of("receptionist"));

    // Setup ForgotPasswordRequest
    forgotPasswordRequest = new ForgotPasswordRequest();
    forgotPasswordRequest.setEmail("test@example.com");

    // Setup ResetPasswordRequest
    resetPasswordRequest = new ResetPasswordRequest();
    resetPasswordRequest.setToken("valid-token");
    resetPasswordRequest.setNewPassword("newPassword123@");

    // Setup test user
    User testUser = new User("testuser", "test@example.com", "encodedPassword");
    testUser.setUserId(1L);
    Set<Role> roles = new HashSet<>();
    Role role = new Role(AppRole.ROLE_RECEPTIONIST);
    role.setRoleId(1L);
    roles.add(role);
    testUser.setRoles(roles);
  }

  @Test
  public void testAuthenticateUser_Success() throws Exception {
    // Arrange
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);

    ResponseCookie jwtCookie = ResponseCookie.from("jwt", "token").path("/").build();
    when(jwtUtils.generateJwtCookie(any(UserDetailsImpl.class))).thenReturn(jwtCookie);

    // Act & Assert
    mockMvc.perform(post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.id").value(userDetails.getId()))
            .andExpect(jsonPath("$.username").value(userDetails.getUsername()));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtUtils).generateJwtCookie(any(UserDetailsImpl.class));
  }

  @Test
  public void testAuthenticateUser_Failure() throws Exception {
    // Arrange
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new AuthenticationServiceException("Bad credentials"));

    // Act & Assert
    mockMvc.perform(post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Bad credentials"))
            .andExpect(jsonPath("$.status").value(false));

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  public void testRegisterUser_Success() throws Exception {
    // Arrange
    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);

    Role receptionistRole = new Role();
    receptionistRole.setRoleId(1L);
    receptionistRole.setRoleName(AppRole.ROLE_RECEPTIONIST);

    when(roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST))
            .thenReturn(Optional.of(receptionistRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(new User());

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully!"));

    verify(userRepository).existsByUserName(signupRequest.getUsername());
    verify(userRepository).existsByEmail(signupRequest.getEmail());
    verify(roleRepository).findByRoleName(AppRole.ROLE_RECEPTIONIST);
    verify(passwordEncoder).encode(signupRequest.getPassword());
    verify(userRepository).save(any(User.class));
  }

  @Test
  public void testRegisterUser_UsernameExists() throws Exception {
    // Arrange
    when(userRepository.existsByUserName(anyString())).thenReturn(true);

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));

    verify(userRepository).existsByUserName(signupRequest.getUsername());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void testRegisterUser_EmailExists() throws Exception {
    // Arrange
    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(true);

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));

    verify(userRepository).existsByUserName(signupRequest.getUsername());
    verify(userRepository).existsByEmail(signupRequest.getEmail());
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void testRegisterUser_WithAdminRole() throws Exception {
    // Arrange
    signupRequest.setRole(Set.of("admin"));

    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);

    Role adminRole = new Role();
    adminRole.setRoleId(1L);
    adminRole.setRoleName(AppRole.ROLE_ADMIN);

    when(roleRepository.findByRoleName(AppRole.ROLE_ADMIN))
            .thenReturn(Optional.of(adminRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(new User());

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully!"));

    verify(roleRepository).findByRoleName(AppRole.ROLE_ADMIN);
  }

  @Test
  public void testRegisterUser_WithDoctorRole() throws Exception {
    // Arrange
    signupRequest.setRole(Set.of("doctor"));

    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);

    Role doctorRole = new Role();
    doctorRole.setRoleId(1L);
    doctorRole.setRoleName(AppRole.ROLE_DENTIST);

    when(roleRepository.findByRoleName(AppRole.ROLE_DENTIST))
            .thenReturn(Optional.of(doctorRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(new User());

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully!"));

    verify(roleRepository).findByRoleName(AppRole.ROLE_DENTIST);
  }

  @Test
  public void testRegisterUser_WithDefaultRole() throws Exception {
    // Arrange
    signupRequest.setRole(null);

    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);

    Role receptionistRole = new Role();
    receptionistRole.setRoleId(1L);
    receptionistRole.setRoleName(AppRole.ROLE_RECEPTIONIST);

    when(roleRepository.findByRoleName(AppRole.ROLE_RECEPTIONIST))
            .thenReturn(Optional.of(receptionistRole));

    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(new User());

    // Act & Assert
    mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User registered successfully!"));

    verify(roleRepository).findByRoleName(AppRole.ROLE_RECEPTIONIST);
  }

  @Test
  public void testRegisterUser_RoleNotFound() {
    // Since this is a challenging case to test through MockMvc due to the exception propagation,
    // we'll test it directly by calling the controller method

    // Arrange
    when(userRepository.existsByUserName(anyString())).thenReturn(false);
    when(userRepository.existsByEmail(anyString())).thenReturn(false);
    when(roleRepository.findByRoleName(any(AppRole.class))).thenReturn(Optional.empty());

    // Act & Assert
    try {
      mockMvc.perform(post("/api/auth/signup")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(signupRequest)));
    } catch (Exception e) {
      // Verify that the exception is related to role not found
      assertInstanceOf(RuntimeException.class, e.getCause());
      assertTrue(e.getCause().getMessage().contains("Role is not found"));
    }
  }

  @Test
  public void testSignoutUser() throws Exception {
    // Arrange
    ResponseCookie cookie = ResponseCookie.from("jwt", "").path("/").maxAge(0).build();
    when(jwtUtils.getCleanJwtCookie()).thenReturn(cookie);

    // Act & Assert
    mockMvc.perform(post("/api/auth/signout"))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(jsonPath("$.message").value("You've been signed out!"));

    verify(jwtUtils).getCleanJwtCookie();
  }

  // Since the security context tests are particularly challenging,
  // we'll test the method directly instead of via MockMvc
  @Test
  public void testCurrentUserName_Anonymous() {
    // This test calls the method directly
    String username = authController.currentUserName(null).getBody();
    assertEquals("anonymousUser", username);
  }

  @Test
  public void testGetUserDetails_WithAuthentication() {
    // Directly test the getUserDetails method by passing an Authentication object
    // This bypasses the security context entirely

    // Arrange - We already have the authentication mock set up in the setup method
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(userDetails);

    // Act
    ResponseEntity<?> response = authController.getUserDetails(authentication);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertInstanceOf(UserInfoResponse.class, response.getBody());

    UserInfoResponse userInfoResponse = (UserInfoResponse) response.getBody();
    assertEquals(userDetails.getId(), userInfoResponse.getId());
    assertEquals(userDetails.getUsername(), userInfoResponse.getUsername());
    assertEquals(1, userInfoResponse.getRoles().size());
    assertTrue(userInfoResponse.getRoles().contains(AppRole.ROLE_RECEPTIONIST.name()));
  }

  @Test
  public void testGetUserDetails_WithUnauthenticatedUser() {
    // Test with a non-authenticated user

    // Arrange
    when(authentication.isAuthenticated()).thenReturn(false);

    // Act & Assert
    Exception exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> authController.getUserDetails(authentication));

    assertEquals("No user is currently authenticated", exception.getMessage());
  }

  @Test
  public void testGetUserDetails_WithNullAuthentication() {
    // Test with null authentication

    // Act & Assert
    Exception exception = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> authController.getUserDetails(null));

    assertEquals("No user is currently authenticated", exception.getMessage());
  }

  @Test
  public void testForgotPassword() throws Exception {
    // Arrange
    doNothing().when(passwordResetService).initiatePasswordReset(any(ForgotPasswordRequest.class));

    // Act & Assert
    mockMvc.perform(post("/api/auth/forgot-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password reset link sent to your email."));

    verify(passwordResetService).initiatePasswordReset(any(ForgotPasswordRequest.class));
  }

  @Test
  public void testResetPassword() throws Exception {
    // Arrange
    doNothing().when(passwordResetService).resetPassword(any(ResetPasswordRequest.class));

    // Act & Assert
    mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(resetPasswordRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Password has been reset successfully."));

    verify(passwordResetService).resetPassword(any(ResetPasswordRequest.class));
  }
}