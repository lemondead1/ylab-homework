package com.lemondead1.carshopservice.exceptions;

public class ParsingException extends CommandException {
  public ParsingException() { }

  public ParsingException(String message) {
    super(message);
  }

  public ParsingException(String message, Throwable cause) {
    super(message, cause);
  }

  public ParsingException(Throwable cause) {
    super(cause);
  }
}
