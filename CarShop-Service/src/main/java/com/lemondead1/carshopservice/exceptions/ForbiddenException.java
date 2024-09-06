package com.lemondead1.carshopservice.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@StandardException
public class ForbiddenException extends ResponseStatusException {
  public ForbiddenException(String reason, Throwable cause) {
    super(HttpStatus.FORBIDDEN, reason, cause);
  }
}
