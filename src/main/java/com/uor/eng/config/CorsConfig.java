package com.uor.eng.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Specify the origins that are allowed to make requests
    configuration.setAllowedOrigins(List.of("http://localhost:5173"));

    // Specify the HTTP methods that are allowed
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

    // Specify the headers that are allowed in requests
    configuration.setAllowedHeaders(List.of("*"));

    // Allow credentials (e.g., cookies, authorization headers)
    configuration.setAllowCredentials(true);

    // Optionally, specify headers that can be exposed to the client
    configuration.setExposedHeaders(List.of("Authorization"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    // Apply the CORS configuration to specific URL patterns
    source.registerCorsConfiguration("/api/**", configuration);

    return source;
  }
}
