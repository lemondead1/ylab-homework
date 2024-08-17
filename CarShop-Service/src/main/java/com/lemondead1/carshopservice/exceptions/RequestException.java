package com.lemondead1.carshopservice.exceptions;

import org.eclipse.jetty.http.HttpException;

public class RequestException extends RuntimeException implements HttpException {
  private final int code;

  @Override
  public int getCode() {
    return code;
  }

  @Override
  public String getReason() {
    return getMessage();
  }

  public RequestException(int code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
  }

  public RequestException(int code, String message) {
    super(message);
    this.code = code;
  }

  public RequestException(int code, Throwable cause) {
    super(cause);
    this.code = code;
  }

  public RequestException(int code) {
    this.code = code;
  }
}
