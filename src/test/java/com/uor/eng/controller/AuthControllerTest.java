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
import com.uor.eng.security.services.UserDetailsImpl;
import com.uor.eng.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private JwtUtils jwtUtils;

  @MockBean
  private AuthenticationManager authenticationManager;

  @MockBean
  private UserRepository userRepository;

  @MockBean
  private RoleRepository roleRepository;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private PasswordResetService passwordResetService;

  private final ObjectMapper objectMapper = new ObjectMapper();
  private Authentication authentication;
  private UserDetailsImpl userDetails;
  private LoginRequest loginRequest;
  private SignupRequest signupRequest;
  private ForgotPasswordRequest forgotPasswordRequest;
  private ResetPasswordRequest resetPasswordRequest;

  @BeforeEach
  public void setup() {
    // Mock Authentication
    authentication = mock(Authentication.class);

    // Setup UserDetails
    List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(AppRole.ROLE_RECEPTIONIST.name())
    );
    userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", authorities);

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
            .thenThrow(new AuthenticationException("Bad credentials") {});

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