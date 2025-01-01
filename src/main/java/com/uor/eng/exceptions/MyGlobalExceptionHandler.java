// src/main/java/com/uor/eng/exceptions/MyGlobalExceptionHandler.java

package com.uor.eng.exceptions;

import com.uor.eng.payload.APIResponse;
import com.uor.eng.payload.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class MyGlobalExceptionHandler extends ResponseEntityExceptionHandler {

  private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, Map<String, String> details) {
    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        status.value(),
        error,
        details
    );
    return new ResponseEntity<>(errorResponse, status);
  }


  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      errors.put(fieldName, message);
    });
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Constraint Violation", errors);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", errors);
  }

  @ExceptionHandler({UnauthorizedAccessException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleUnauthorizedAccessException(Exception ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.FORBIDDEN, "Access Denied", errors);
  }

  @ExceptionHandler({BadRequestException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResponse> handleBadRequestException(Exception ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", errors);
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<ErrorResponse> handleInternalServerErrorException(InternalServerErrorException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errors);
  }

  @ExceptionHandler(APIException.class)
  public ResponseEntity<APIResponse> handleAPIException(APIException ex) {
    APIResponse apiResponse = new APIResponse(ex.getMessage(), false);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
    String rootCauseMessage = ex.getMostSpecificCause().getMessage();
    log.error("DataIntegrityViolationException: {}", rootCauseMessage);

    String message = "Data integrity violation";
    if (rootCauseMessage.contains("UK_dentist_nic") || rootCauseMessage.contains("UK_receptionist_nic")) {
      message = "NIC is already in use!";
    } else if (rootCauseMessage.contains("UK_dentist_license_number")) {
      message = "License number is already in use!";
    } else if (rootCauseMessage.contains("UK_receptionist_employee_id")) {
      message = "Employee ID is already in use!";
    } else if (rootCauseMessage.contains("userName")) {
      message = "Username is already taken!";
    } else if (rootCauseMessage.contains("email")) {
      message = "Email is already in use!";
    }

    Map<String, String> errors = new HashMap<>();
    errors.put("error", message);
    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Data Integrity Violation", errors);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", errors);
  }

  @ExceptionHandler(EmailSendingException.class)
  public ResponseEntity<ErrorResponse> handleEmailSendingException(EmailSendingException ex) {
    Map<String, String> errors = new HashMap<>();
    errors.put("error", ex.getMessage());
    return buildErrorResponse(HttpStatus.SERVICE_UNAVAILABLE, "Email Sending Failed", errors);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
    log.error("Unhandled exception: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    errors.put("error", "An unexpected error occurred");
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", errors);
  }

  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
    log.error("DataAccessException: {}", ex.getMessage(), ex);
    Map<String, String> errors = new HashMap<>();
    errors.put("error", "Database error occurred");
    return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error", errors);
  }
}
