package com.uor.eng.exceptions;

import com.uor.eng.payload.other.ErrorResponse;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyGlobalExceptionHandlerTest {

  @InjectMocks
  private MyGlobalExceptionHandler exceptionHandler;

  @Mock
  private ConstraintViolation<Object> violation;

  @Mock
  private MethodArgumentNotValidException methodArgumentNotValidException;

  @Mock
  private BindingResult bindingResult;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testHandleConstraintViolationException() {
    // Arrange
    Set<ConstraintViolation<Object>> violations = new HashSet<>();
    violations.add(violation);

    ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

    when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
    when(violation.getPropertyPath().toString()).thenReturn("fieldName");
    when(violation.getMessage()).thenReturn("field error message");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Constraint Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("field error message", details.get("fieldName"));
  }

  @Test
  void testHandleResourceNotFoundException() {
    // Arrange
    String errorMessage = "Resource not found";
    ResourceNotFoundException ex = new ResourceNotFoundException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(ex);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Resource Not Found", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleBadRequestException() {
    // Arrange
    String errorMessage = "Bad request";
    BadRequestException ex = new BadRequestException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Bad Request", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleMethodArgumentTypeMismatchException() {
    // Arrange
    String errorMessage = "Type mismatch";
    MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
    when(ex.getMessage()).thenReturn(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadRequestException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Bad Request", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleInternalServerErrorException() {
    // Arrange
    String errorMessage = "Internal server error";
    InternalServerErrorException ex = new InternalServerErrorException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleInternalServerErrorException(ex);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Internal Server Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleAPIException() {
    // Arrange
    String errorMessage = "API error";
    APIException ex = new APIException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleAPIException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("API Exception", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_NIC() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Error: UK_dentist_nic constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("NIC is already in use!", details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_LicenseNumber() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Error: UK_dentist_license_number constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("License number is already in use!", details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_EmployeeId() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Error: UK_receptionist_employee_id constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Employee ID is already in use!", details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_UserName() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Error: userName constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Username is already taken!", details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_Email() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Error: email constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Email is already in use!", details.get("error"));
  }

  @Test
  void testHandleDataIntegrityViolationException_Generic() {
    // Arrange
    DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
    Throwable cause = mock(Throwable.class);
    when(ex.getMostSpecificCause()).thenReturn(cause);
    when(cause.getMessage()).thenReturn("Some other constraint violation");

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolationException(ex);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Data Integrity Violation", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Data integrity violation", details.get("error"));
  }

  @Test
  void testHandleJwtException() {
    // Arrange
    String errorMessage = "Invalid token";
    JwtException ex = new JwtException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleJwtException(ex);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("JWT Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Invalid or expired JWT token", details.get("error"));
  }

  @Test
  void testHandleGlobalException() {
    // Arrange
    String errorMessage = "Unexpected error";
    Exception ex = new Exception(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(ex);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Internal Server Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("An unexpected error occurred", details.get("error"));
  }

  @Test
  void testHandleDataAccessException() {
    // Arrange
    String errorMessage = "Database error";
    DataAccessException ex = new DataAccessException(errorMessage) {
    };

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataAccessException(ex);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Database Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("Database error occurred", details.get("error"));
  }

  @Test
  void testHandleAuthenticationCredentialsNotFoundException() {
    // Arrange
    String errorMessage = "Authentication required";
    AuthenticationCredentialsNotFoundException ex = new AuthenticationCredentialsNotFoundException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthenticationCredentialsNotFoundException(ex);

    // Assert
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Authentication Required", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleMethodArgumentNotValidException() {
    // Arrange
    FieldError fieldError = new FieldError("objectName", "fieldName", "field error message");
    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodArgumentNotValidException(methodArgumentNotValidException);

    // Assert
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("Validation Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals("field error message", details.get("fieldName"));
  }

  @Test
  void testHandleFileStorageException() {
    // Arrange
    String errorMessage = "File storage error";
    FileStorageException ex = new FileStorageException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileStorageException(ex);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("File Storage Error", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }

  @Test
  void testHandleFileNotFoundException() {
    // Arrange
    String errorMessage = "File not found";
    FileNotFoundException ex = new FileNotFoundException(errorMessage);

    // Act
    ResponseEntity<ErrorResponse> response = exceptionHandler.handleFileNotFoundException(ex);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    ErrorResponse errorResponse = response.getBody();
    assertNotNull(errorResponse);
    assertEquals("File Not Found", errorResponse.getError());
    Map<String, String> details = errorResponse.getDetails();
    assertEquals(errorMessage, details.get("error"));
  }
}