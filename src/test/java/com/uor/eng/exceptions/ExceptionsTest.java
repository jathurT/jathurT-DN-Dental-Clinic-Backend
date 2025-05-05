package com.uor.eng.exceptions;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

  @Test
  void testAPIException() {
    // Test constructor with message
    String errorMessage = "API Error occurred";
    APIException exception = new APIException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertTrue(true);
  }

  @Test
  void testBadRequestException() {
    // Test constructor with message
    String errorMessage = "Bad request detected";
    BadRequestException exception = new BadRequestException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertTrue(true);
  }

  @Test
  void testEmailSendingException() {
    // Test constructor with message
    String errorMessage = "Failed to send email";
    EmailSendingException exception = new EmailSendingException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertTrue(true);
  }

  @Test
  void testFileStorageException() {
    // Test constructor with message only
    String errorMessage = "File storage error";
    FileStorageException exception1 = new FileStorageException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception1.getMessage());

    // Test constructor with message and cause
    Throwable cause = new IOException("IO Error");
    FileStorageException exception2 = new FileStorageException(errorMessage, cause);

    // Verify message and cause are correctly stored
    assertEquals(errorMessage, exception2.getMessage());
    assertEquals(cause, exception2.getCause());

    // Verify inheritance
    assertTrue(true);
    assertTrue(true);
  }

  @Test
  void testInternalServerErrorException() {
    // Test constructor with message
    String errorMessage = "Internal server error";
    InternalServerErrorException exception = new InternalServerErrorException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertInstanceOf(RuntimeException.class, exception);
  }

  @Test
  void testResourceNotFoundException() {
    // Test constructor with message
    String errorMessage = "Resource not found";
    ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertTrue(true);
  }

  @Test
  void testUnauthorizedAccessException() {
    // Test constructor with message
    String errorMessage = "Unauthorized access";
    UnauthorizedAccessException exception = new UnauthorizedAccessException(errorMessage);

    // Verify message is correctly stored
    assertEquals(errorMessage, exception.getMessage());

    // Verify inheritance
    assertTrue(true);
  }
}