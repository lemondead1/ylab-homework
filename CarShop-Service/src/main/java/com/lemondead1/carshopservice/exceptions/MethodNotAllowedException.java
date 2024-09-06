package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class MethodNotAllowedException extends RequestException {
  public MethodNotAllowedException(String message) {
    super(HttpStatus.METHOD_NOT_ALLOWED_405, message);
  }
}
