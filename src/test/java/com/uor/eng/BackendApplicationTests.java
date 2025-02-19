package com.uor.eng;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")  // This will use application-test.properties if you prefer that approach
public class BackendApplicationTests {

  @Test
  void contextLoads() {
    // This will now use the H2 in-memory database configuration
  }
}