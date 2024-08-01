package com.lemondead1.carshopservice.service;

public class LoggerService {
  public void print(String string) {
    System.err.print(string);
  }

  public void println(String string) {
    System.err.println(string);
  }

  public void printf(String string, Object... args) {
    System.err.printf(string, args);
  }
}
