package com.lemondead1.carshopservice.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@StandardException
public class BadRequestException extends ResponseStatusException {
  public BadRequestException(String reason, Throwable cause) {
    super(HttpStatus.BAD_REQUEST, reason, cause);
  }
}
