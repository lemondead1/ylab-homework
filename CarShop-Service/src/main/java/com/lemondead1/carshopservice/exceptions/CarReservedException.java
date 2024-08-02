package com.lemondead1.carshopservice.exceptions;

public class CarReservedException extends CommandException {
  public CarReservedException() {
  }

  public CarReservedException(String message) {
    super(message);
  }

  public CarReservedException(String message, Throwable cause) {
    super(message, cause);
  }

  public CarReservedException(Throwable cause) {
    super(cause);
  }
}
