package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class ConflictException extends RequestException {
  public ConflictException(String message) {
    super(HttpStatus.CONFLICT_409, message);
  }
}
