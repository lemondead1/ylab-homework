package com.lemondead1.carshopservice.exceptions;

import lombok.experimental.StandardException;

@StandardException
public class DBException extends InternalServerErrorException {
  public DBException(String reason, Throwable cause) {
    super(reason, cause);
  }
}
