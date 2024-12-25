package com.uor.eng.exceptions;

import com.uor.eng.payload.APIResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class MyGlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException e) {
    Map<String, String> response = new HashMap<>();
    e.getBindingResult().getAllErrors().forEach(err -> {
      String fieldName = ((FieldError) err).getField();
      String message = err.getDefaultMessage();
      response.put(fieldName, message);
    });
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException e) {
    Map<String, String> response = new HashMap<>();
    e.getConstraintViolations().forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      String message = violation.getMessage();
      response.put(fieldName, message);
    });
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedAccessException.class)
  public ResponseEntity<Map<String, String>> handleUnauthorizedAccessException(UnauthorizedAccessException e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(InternalServerErrorException.class)
  public ResponseEntity<Map<String, String>> handleInternalServerErrorException(InternalServerErrorException e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGlobalException(Exception e) {
    Map<String, String> response = new HashMap<>();
    response.put("error", "An unexpected error occurred: " + e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(APIException.class)
  public ResponseEntity<APIResponse> myAPIException(APIException e) {
    String message = e.getMessage();
    APIResponse apiResponse = new APIResponse(message, false);
    return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    Map<String, String> response = new HashMap<>();
    String message = "Data integrity violation";
    String rootCauseMessage = e.getMostSpecificCause().getMessage();
    log.error("DataIntegrityViolationException: {}", rootCauseMessage);
    if (rootCauseMessage.contains("UK_dentist_nic")) {
      message = "NIC is already in use!";
    } else if (rootCauseMessage.contains("UK_dentist_license_number")) {
      message = "License number is already in use!";
    } else if (rootCauseMessage.contains("UK_receptionist_nic")) {
      message = "NIC is already in use!";
    } else if (rootCauseMessage.contains("UK_receptionist_employee_id")) {
      message = "Employee ID is already in use!";
    } else if (rootCauseMessage.contains("userName")) {
      message = "Username is already taken!";
    } else if (rootCauseMessage.contains("email")) {
      message = "Email is already in use!";
    }

    response.put("error", message);
    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

}
