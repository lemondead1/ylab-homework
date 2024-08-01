package com.lemondead1.carshopservice.exceptions;

public class UsernameUsedException extends CommandException {
  public UsernameUsedException() { }

  public UsernameUsedException(String message) {
    super(message);
  }

  public UsernameUsedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UsernameUsedException(Throwable cause) {
    super(cause);
  }
}
