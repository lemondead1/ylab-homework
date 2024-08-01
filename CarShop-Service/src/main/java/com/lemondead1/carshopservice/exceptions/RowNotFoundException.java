package com.lemondead1.carshopservice.exceptions;

public class RowNotFoundException extends CommandException {
  public RowNotFoundException() { }

  public RowNotFoundException(String message) {
    super(message);
  }

  public RowNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public RowNotFoundException(Throwable cause) {
    super(cause);
  }
}
