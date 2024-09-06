package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class UserAlreadyExistsException extends RequestException {
  public UserAlreadyExistsException(String message) {
    super(HttpStatus.CONFLICT_409, message);
  }
}
