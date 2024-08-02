package com.lemondead1.carshopservice.exceptions;

public class ForeignKeyException extends CommandException {
  public ForeignKeyException() {
  }

  public ForeignKeyException(String message) {
    super(message);
  }

  public ForeignKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  public ForeignKeyException(Throwable cause) {
    super(cause);
  }
}
