package com.lemondead1.carshopservice.exceptions;

public class WrongUsernamePassword extends CommandException {
  public WrongUsernamePassword() { }

  public WrongUsernamePassword(String message) {
    super(message);
  }

  public WrongUsernamePassword(String message, Throwable cause) {
    super(message, cause);
  }

  public WrongUsernamePassword(Throwable cause) {
    super(cause);
  }
}
