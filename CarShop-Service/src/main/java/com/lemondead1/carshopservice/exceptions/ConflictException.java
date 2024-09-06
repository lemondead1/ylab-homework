package com.lemondead1.carshopservice.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@StandardException
public class ConflictException extends ResponseStatusException {
  public ConflictException(String reason, Throwable cause) {
    super(HttpStatus.CONFLICT, reason, cause);
  }
}
