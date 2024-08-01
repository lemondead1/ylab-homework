package com.lemondead1.carshopservice.exceptions;

public class DumpException extends CommandException {
  public DumpException() { }

  public DumpException(String message) {
    super(message);
  }

  public DumpException(String message, Throwable cause) {
    super(message, cause);
  }

  public DumpException(Throwable cause) {
    super(cause);
  }
}
