package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class ValidationException extends RequestException {
  public ValidationException(String message) {
    super(HttpStatus.BAD_REQUEST_400, message);
  }
}
