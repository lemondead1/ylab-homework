package com.lemondead1.carshopservice.exceptions;

public class CascadingException extends CommandException {
  public CascadingException() {
  }

  public CascadingException(String message) {
    super(message);
  }

  public CascadingException(String message, Throwable cause) {
    super(message, cause);
  }

  public CascadingException(Throwable cause) {
    super(cause);
  }
}
