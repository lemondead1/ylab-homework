package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class NotFoundException extends RequestException {
  public NotFoundException(String message) {
    super(HttpStatus.NOT_FOUND_404, message);
  }

  public NotFoundException(String message, Throwable cause) {
    super(HttpStatus.NOT_FOUND_404, message, cause);
  }
}
