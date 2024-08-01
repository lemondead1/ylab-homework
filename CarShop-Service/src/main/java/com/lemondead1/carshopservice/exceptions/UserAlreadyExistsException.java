package com.lemondead1.carshopservice.exceptions;

public class UserAlreadyExistsException extends CommandException {
  public UserAlreadyExistsException() { }

  public UserAlreadyExistsException(String message) {
    super(message);
  }

  public UserAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserAlreadyExistsException(Throwable cause) {
    super(cause);
  }
}
