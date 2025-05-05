package com.uor.eng.security.jwt;

import com.uor.eng.security.services.UserDetailsImpl;
import com.uor.eng.security.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthTokenFilterTest {

  @InjectMocks
  private AuthTokenFilter authTokenFilter;

  @Mock
  private JwtUtils jwtUtils;

  @Mock
  private UserDetailsServiceImpl userDetailsService;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain filterChain;

  @BeforeEach
  void setUp() {
    SecurityContext securityContext = new SecurityContextImpl();
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void doFilterInternalShouldSetAuthenticationWhenValidToken() throws ServletException, IOException {
    // Arrange
    String token = "valid-jwt-token";
    String username = "testuser";

    when(jwtUtils.getJwtFromCookies(request)).thenReturn(token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);

    UserDetailsImpl userDetails = new UserDetailsImpl(
            1L,
            username,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(userDetailsService).loadUserByUsername(username);

    // Verify that authentication was set in security context
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    assertNotNull(authentication);
    assertEquals(username, ((UserDetailsImpl) authentication.getPrincipal()).getUsername());
  }

  @Test
  void doFilterInternalShouldNotSetAuthenticationWhenNoToken() throws ServletException, IOException {
    // Arrange
    when(jwtUtils.getJwtFromCookies(request)).thenReturn(null);

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalShouldNotSetAuthenticationWhenInvalidToken() throws ServletException, IOException {
    // Arrange
    String token = "invalid-jwt-token";
    when(jwtUtils.getJwtFromCookies(request)).thenReturn(token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(false);

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalShouldHandleExceptionFromValidation() throws ServletException, IOException {
    // Arrange
    String token = "problematic-token";
    when(jwtUtils.getJwtFromCookies(request)).thenReturn(token);
    when(jwtUtils.validateJwtToken(token)).thenThrow(new RuntimeException("Validation error"));

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verifyNoInteractions(userDetailsService);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalShouldHandleExceptionFromUserDetailsService() throws ServletException, IOException {
    // Arrange
    String token = "valid-jwt-token";
    String username = "testuser";

    when(jwtUtils.getJwtFromCookies(request)).thenReturn(token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);
    when(userDetailsService.loadUserByUsername(username)).thenThrow(new RuntimeException("User not found"));

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(userDetailsService).loadUserByUsername(username);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void doFilterInternalShouldHandleExceptionDuringAuthenticationSetting() throws ServletException, IOException {
    // Arrange
    String token = "valid-jwt-token";
    String username = "testuser";

    when(jwtUtils.getJwtFromCookies(request)).thenReturn(token);
    when(jwtUtils.validateJwtToken(token)).thenReturn(true);
    when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn(username);

    UserDetailsImpl userDetails = new UserDetailsImpl(
            1L,
            username,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
    when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

    // Set up a SecurityContext that will throw an exception when setting authentication
    SecurityContext mockContext = mock(SecurityContext.class);
    doThrow(new RuntimeException("Authentication error")).when(mockContext).setAuthentication(any());
    SecurityContextHolder.setContext(mockContext);

    // Act
    authTokenFilter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    verify(userDetailsService).loadUserByUsername(username);
    verify(mockContext).setAuthentication(any());
  }
}