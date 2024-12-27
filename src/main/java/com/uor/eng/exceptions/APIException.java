package com.uor.eng.exceptions;

public class APIException extends RuntimeException {
  public APIException(String message) {
    super(message);
  }

  public APIException(String message, Throwable cause) {
    super(message, cause);
  }
}
