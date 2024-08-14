package com.lemondead1.carshopservice.cli;

import lombok.RequiredArgsConstructor;

import java.io.Console;

/**
 * Console decorator with support for parsing
 */
@RequiredArgsConstructor
public class ConsoleCLI extends CLI {
  private final Console io;

  @Override
  public void printf(String pattern, Object... args) {
    io.printf(pattern, args);
  }

  @Override
  public String readInteractive(String message) {
    return io.readLine(message);
  }

  @Override
  public String readPassword(String message) {
    return new String(io.readPassword(message));
  }
}
