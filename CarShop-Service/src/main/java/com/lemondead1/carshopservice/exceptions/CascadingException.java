package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class CascadingException extends RequestException {
  public CascadingException(String message) {
    super(HttpStatus.CONFLICT_409, message);
  }
}
