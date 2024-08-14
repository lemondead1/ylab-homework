package com.lemondead1.carshopservice.cli;

import java.util.Scanner;

public class PlainCLI extends CLI {
  private final Scanner in = new Scanner(System.in);

  @Override
  public void printf(String pattern, Object... args) {
    System.out.printf(pattern, args);
  }

  @Override
  public String readInteractive(String message) {
    System.out.print(message);
    return in.nextLine();
  }

  @Override
  public String readPassword(String message) {
    return readInteractive(message);
  }
}
