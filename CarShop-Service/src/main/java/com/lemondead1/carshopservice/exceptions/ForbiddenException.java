package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class ForbiddenException extends RequestException {
  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN_403, message);
  }
}
