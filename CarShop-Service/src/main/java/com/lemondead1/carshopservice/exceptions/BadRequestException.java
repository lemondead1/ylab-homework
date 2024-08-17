package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpStatus;

public class BadRequestException extends RequestException {
  public BadRequestException(String message) {
    super(HttpStatus.BAD_REQUEST_400, message);
  }
}
