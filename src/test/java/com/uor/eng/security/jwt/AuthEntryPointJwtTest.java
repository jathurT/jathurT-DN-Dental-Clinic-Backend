package com.uor.eng.security.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

  @InjectMocks
  private AuthEntryPointJwt authEntryPointJwt;

  @Mock
  private HttpServletRequest request;

  @Mock
  private AuthenticationException authException;

  private MockHttpServletResponse response;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    response = new MockHttpServletResponse();
    objectMapper = new ObjectMapper();
  }

  @Test
  void commenceShouldSetCorrectStatusAndContentType() throws Exception {
    // Arrange
    when(authException.getMessage()).thenReturn("Authentication failed");
    when(request.getServletPath()).thenReturn("/api/test");

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
  }

  @Test
  void commenceShouldReturnCorrectJsonBodyWithAuthExceptionMessage() throws Exception {
    // Arrange
    when(authException.getMessage()).thenReturn("Authentication failed");
    when(request.getServletPath()).thenReturn("/api/test");

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    String responseContent = response.getContentAsString();
    JsonNode jsonResponse = objectMapper.readTree(responseContent);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, jsonResponse.get("status").asInt());
    assertEquals("Unauthorized", jsonResponse.get("error").asText());
    assertEquals("Authentication failed", jsonResponse.get("message").asText());
    assertEquals("/api/test", jsonResponse.get("path").asText());
  }

  @Test
  void commenceShouldHandleBadCredentialsException() throws Exception {
    // Arrange
    BadCredentialsException badCredentialsException = new BadCredentialsException("Invalid credentials");
    when(request.getServletPath()).thenReturn("/api/auth/signin");

    // Act
    authEntryPointJwt.commence(request, response, badCredentialsException);

    // Assert
    String responseContent = response.getContentAsString();
    JsonNode jsonResponse = objectMapper.readTree(responseContent);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, jsonResponse.get("status").asInt());
    assertEquals("Unauthorized", jsonResponse.get("error").asText());
    assertEquals("Invalid credentials", jsonResponse.get("message").asText());
    assertEquals("/api/auth/signin", jsonResponse.get("path").asText());
  }

  @Test
  void commenceShouldHandleNullAuthExceptionMessage() throws Exception {
    // Arrange
    when(authException.getMessage()).thenReturn(null);
    when(request.getServletPath()).thenReturn("/api/resource");

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    String responseContent = response.getContentAsString();
    JsonNode jsonResponse = objectMapper.readTree(responseContent);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, jsonResponse.get("status").asInt());
    assertEquals("Unauthorized", jsonResponse.get("error").asText());
    assertNull(jsonResponse.get("message").asText(null));
    assertEquals("/api/resource", jsonResponse.get("path").asText());
  }

  @Test
  void commenceShouldHandleComplexRequestPath() throws Exception {
    // Arrange
    when(authException.getMessage()).thenReturn("Authentication failed");
    when(request.getServletPath()).thenReturn("/api/users/123/profile?include=details&format=full");

    // Act
    authEntryPointJwt.commence(request, response, authException);

    // Assert
    String responseContent = response.getContentAsString();
    JsonNode jsonResponse = objectMapper.readTree(responseContent);

    assertEquals("/api/users/123/profile?include=details&format=full", jsonResponse.get("path").asText());
  }
}