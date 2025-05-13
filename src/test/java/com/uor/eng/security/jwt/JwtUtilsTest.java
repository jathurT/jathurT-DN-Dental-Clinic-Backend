package com.uor.eng.security.jwt;

import com.uor.eng.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtUtilsTest {

  @InjectMocks
  private JwtUtils jwtUtils;

  @Mock
  private HttpServletRequest request;

  private UserDetailsImpl userDetails;
  private final String username = "testuser";
  private final String jwtCookieName = "testCookie";
  private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
  private final String jwtSecret = Base64.getEncoder().encodeToString(secretKey.getEncoded());
  private final int jwtExpirationMs = 3600000; // 1 hour

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(jwtUtils, "jwtCookie", jwtCookieName);
    ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
    ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);

    // DO NOT try to mock the Logger field as it's likely static final

    userDetails = new UserDetailsImpl(
            1L,
            username,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
  }

  @Test
  void getJwtFromCookiesShouldReturnTokenWhenCookieExists() {
    // Arrange
    Cookie cookie = new Cookie(jwtCookieName, "token-value");

    try (MockedStatic<WebUtils> webUtils = mockStatic(WebUtils.class)) {
      webUtils.when(() -> WebUtils.getCookie(request, jwtCookieName)).thenReturn(cookie);

      // Act
      String token = jwtUtils.getJwtFromCookies(request);

      // Assert
      assertEquals("token-value", token);
      webUtils.verify(() -> WebUtils.getCookie(request, jwtCookieName));
    }
  }

  @Test
  void getJwtFromCookiesShouldReturnNullWhenCookieDoesNotExist() {
    // Arrange
    try (MockedStatic<WebUtils> webUtils = mockStatic(WebUtils.class)) {
      webUtils.when(() -> WebUtils.getCookie(request, jwtCookieName)).thenReturn(null);

      // Act
      String token = jwtUtils.getJwtFromCookies(request);

      // Assert
      assertNull(token);
      webUtils.verify(() -> WebUtils.getCookie(request, jwtCookieName));
    }
  }

  @Test
  void generateTokenFromUsernameShouldCreateValidToken() {
    // Act
    String token = jwtUtils.generateTokenFromUsername(username);

    // Assert
    assertNotNull(token);
    assertTrue(jwtUtils.validateJwtToken(token));
    assertEquals(username, jwtUtils.getUserNameFromJwtToken(token));
  }

  @Test
  void getUserNameFromJwtTokenShouldExtractSubject() {
    // Arrange
    String token = Jwts.builder()
            .setSubject(username)
            .signWith(secretKey)
            .compact();

    // Act
    String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

    // Assert
    assertEquals(username, extractedUsername);
  }

  @Test
  void generateJwtCookieShouldCreateCookieWithToken() {
    // Act
    ResponseCookie cookie = jwtUtils.generateJwtCookie(userDetails);

    // Assert
    assertNotNull(cookie);
    assertTrue(cookie.toString().contains(jwtCookieName));
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals("Strict", cookie.getSameSite());
    assertEquals("/", cookie.getPath());
    assertEquals(24 * 60 * 60, cookie.getMaxAge().getSeconds());
  }

  @Test
  void getCleanJwtCookieShouldCreateEmptyCookie() {
    // Act
    ResponseCookie cookie = jwtUtils.getCleanJwtCookie();

    // Assert
    assertNotNull(cookie);
    assertTrue(cookie.toString().contains(jwtCookieName));
    assertTrue(cookie.isHttpOnly());
    assertTrue(cookie.isSecure());
    assertEquals("Strict", cookie.getSameSite());
    assertEquals("/", cookie.getPath());
    assertEquals(0, cookie.getMaxAge().getSeconds());
  }

  @Test
  void validateJwtTokenShouldReturnTrueForValidToken() {
    // Arrange
    String token = Jwts.builder()
            .setSubject(username)
            .signWith(secretKey)
            .compact();

    // Act & Assert
    assertTrue(jwtUtils.validateJwtToken(token));
  }

  @Test
  void validateJwtTokenShouldReturnFalseForMalformedToken() {
    // Arrange
    String malformedToken = "invalid.token.format";

    // Act
    boolean result = jwtUtils.validateJwtToken(malformedToken);

    // Assert
    assertFalse(result);
    // Cannot verify log interactions without mocking the logger
  }

  @Test
  void validateJwtTokenShouldReturnFalseForExpiredToken() {
    // Arrange
    String expiredToken = Jwts.builder()
            .setSubject(username)
            .setExpiration(new Date(System.currentTimeMillis() - 1000)) // Set to past time
            .signWith(secretKey)
            .compact();

    // Act
    boolean result = jwtUtils.validateJwtToken(expiredToken);

    // Assert
    assertFalse(result);
    // Cannot verify log interactions without mocking the logger
  }

  @Test
  void validateJwtTokenShouldReturnFalseForUnsupportedToken() {
    // This test is challenging without mocking the logger
    // A better approach is to test with a real token that would cause an UnsupportedJwtException

    // Arrange - One way to create an unsupported token is to use a different algorithm than expected
    String header = Base64.getEncoder().encodeToString("{\"alg\":\"none\"}".getBytes());
    String payload = Base64.getEncoder().encodeToString(
            ("{\"sub\":\"" + username + "\"}").getBytes());
    String unsupportedToken = header + "." + payload + ".";

    // Act
    boolean result = jwtUtils.validateJwtToken(unsupportedToken);

    // Assert
    assertFalse(result);
  }

  @Test
  void validateJwtTokenShouldReturnFalseForEmptyClaimsToken() {
    // Arrange - Testing with an empty string
    String emptyToken = "";

    // Act
    boolean result = jwtUtils.validateJwtToken(emptyToken);

    // Assert
    assertFalse(result);
  }

  @Test
  void validateJwtTokenShouldReturnFalseForInvalidSignature() {
    // Arrange
    // Create a token with a different signing key
    SecretKey differentKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    String tokenWithDifferentKey = Jwts.builder()
            .setSubject(username)
            .signWith(differentKey)
            .compact();

    // Act
    boolean result = jwtUtils.validateJwtToken(tokenWithDifferentKey);

    // Assert
    assertFalse(result);
  }
}