package com.uor.eng.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class CorsConfigTest {

  @InjectMocks
  private CorsConfig corsConfig;

  @BeforeEach
  public void setup() {
    // Default allowed origins for most tests
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "http://localhost:3000,https://example.com");
  }

  @Test
  public void testCorsConfigurationSource() {
    // Act
    CorsConfigurationSource configSource = corsConfig.corsConfigurationSource();

    // Assert
    assertNotNull(configSource, "CorsConfigurationSource should not be null");
    assertTrue(configSource instanceof UrlBasedCorsConfigurationSource,
            "CorsConfigurationSource should be an instance of UrlBasedCorsConfigurationSource");
  }

  @Test
  public void testAllowedOrigins() {
    // Arrange
    String allowedOriginsValue = "https://test1.com,https://test2.com,https://test3.com";
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", allowedOriginsValue);

    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedOrigins = Arrays.asList(allowedOriginsValue.split(","));
    assertEquals(expectedOrigins, corsConfiguration.getAllowedOrigins(),
            "Allowed origins should match the configured values");
    assertEquals(3, corsConfiguration.getAllowedOrigins().size(),
            "Should have exactly 3 allowed origins");
  }

  @Test
  public void testSingleAllowedOrigin() {
    // Arrange
    String singleOrigin = "https://singleorigin.com";
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", singleOrigin);

    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedOrigins = Collections.singletonList(singleOrigin);
    assertEquals(expectedOrigins, corsConfiguration.getAllowedOrigins(),
            "Single allowed origin should be properly configured");
  }

  @Test
  public void testAllowedMethods() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedMethods = Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");
    assertTrue(corsConfiguration.getAllowedMethods().containsAll(expectedMethods),
            "All expected HTTP methods should be allowed");
    assertEquals(expectedMethods.size(), corsConfiguration.getAllowedMethods().size(),
            "Should have exactly the expected number of allowed methods");
  }

  @Test
  public void testAllowedHeaders() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedHeaders = Arrays.asList(
            "Authorization", "Content-Type", "X-Requested-With",
            "Accept", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers");

    assertTrue(corsConfiguration.getAllowedHeaders().containsAll(expectedHeaders),
            "All expected headers should be allowed");
    assertEquals(expectedHeaders.size(), corsConfiguration.getAllowedHeaders().size(),
            "Should have exactly the expected number of allowed headers");
  }

  @Test
  public void testExposedHeaders() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedExposedHeaders = Collections.singletonList("Authorization");
    assertEquals(expectedExposedHeaders, corsConfiguration.getExposedHeaders(),
            "Exposed headers should match expected values");
  }

  @Test
  public void testAllowCredentials() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    assertTrue(corsConfiguration.getAllowCredentials(),
            "Allow credentials should be true");
  }

  @Test
  public void testMaxAge() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    assertEquals(Long.valueOf(3600L), corsConfiguration.getMaxAge(),
            "Max age should be 3600 seconds (1 hour)");
  }

  @Test
  public void testPathMapping() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();

    // Assert
    assertNotNull(configSource.getCorsConfigurations().get("/**"),
            "CORS configuration should be registered for '/**' path pattern");
  }

  @Test
  public void testEmptyAllowedOrigins() {
    // Arrange
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "");

    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    assertEquals(Collections.singletonList(""), corsConfiguration.getAllowedOrigins(),
            "Empty allowed origins should result in a list with an empty string");
  }

  @Test
  public void testSpacesInAllowedOrigins() {
    // Arrange
    String originsWithSpaces = "https://test1.com, https://test2.com, https://test3.com";
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", originsWithSpaces);

    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    List<String> expectedOrigins = Arrays.asList(originsWithSpaces.split(","));
    assertEquals(expectedOrigins, corsConfiguration.getAllowedOrigins(),
            "Should preserve spaces from the original allowed origins string");
    // The origins will contain spaces which would technically be invalid for actual CORS usage
    assertTrue(corsConfiguration.getAllowedOrigins().contains(" https://test2.com"),
            "Should preserve the leading space from the original string");
  }

  @Test
  public void testWildcardAllowedOrigin() {
    // Arrange
    ReflectionTestUtils.setField(corsConfig, "allowedOrigins", "*");

    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert
    assertEquals(Collections.singletonList("*"), corsConfiguration.getAllowedOrigins(),
            "Wildcard allowed origin should be properly configured");
  }

  @Test
  public void testCompleteConfiguration() {
    // Act
    UrlBasedCorsConfigurationSource configSource =
            (UrlBasedCorsConfigurationSource) corsConfig.corsConfigurationSource();
    CorsConfiguration corsConfiguration = configSource.getCorsConfigurations().get("/**");

    // Assert - verify all aspects of the configuration
    assertNotNull(corsConfiguration.getAllowedOrigins(), "Allowed origins should not be null");
    assertNotNull(corsConfiguration.getAllowedMethods(), "Allowed methods should not be null");
    assertNotNull(corsConfiguration.getAllowedHeaders(), "Allowed headers should not be null");
    assertNotNull(corsConfiguration.getExposedHeaders(), "Exposed headers should not be null");
    assertTrue(corsConfiguration.getAllowCredentials(), "Allow credentials should be true");
    assertEquals(Long.valueOf(3600L), corsConfiguration.getMaxAge(), "Max age should be 3600 seconds");
  }
}