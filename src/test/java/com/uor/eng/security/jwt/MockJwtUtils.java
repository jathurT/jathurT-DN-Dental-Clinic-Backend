package com.uor.eng.security.jwt;

import com.uor.eng.security.services.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

/**
 * Mock implementation of JwtUtils for testing purposes.
 * This avoids dependency on @Value annotations that might cause issues in tests.
 */
public class MockJwtUtils extends JwtUtils {

  private String jwtSecret = "testsecretkey12345678901234567890";
  private Integer jwtExpirationMs = 3600000;
  private String jwtCookie = "test-cookie";

  @Override
  public String getJwtFromCookies(HttpServletRequest request) {
    return "test-token";
  }

  @Override
  public ResponseCookie generateJwtCookie(UserDetailsImpl userPrincipal) {
    return ResponseCookie.from(jwtCookie, "test-token")
            .path("/")
            .maxAge(24 * 60 * 60)
            .httpOnly(true)
            .build();
  }

  @Override
  public ResponseCookie getCleanJwtCookie() {
    return ResponseCookie.from(jwtCookie, "")
            .path("/")
            .maxAge(0)
            .httpOnly(true)
            .build();
  }

  @Override
  public String getUserNameFromJwtToken(String token) {
    return "testuser";
  }

  @Override
  public boolean validateJwtToken(String authToken) {
    return true;
  }
}