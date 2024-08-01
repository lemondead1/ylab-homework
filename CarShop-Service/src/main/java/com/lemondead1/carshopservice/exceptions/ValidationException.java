package com.lemondead1.carshopservice.exceptions;

public class ValidationException extends CommandException {
  public ValidationException() { }

  public ValidationException(String message) {
    super(message);
  }

  public ValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ValidationException(Throwable cause) {
    super(cause);
  }
}
