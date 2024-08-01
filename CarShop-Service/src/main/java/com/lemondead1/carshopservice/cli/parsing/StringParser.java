package com.lemondead1.carshopservice.cli.parsing;

public enum StringParser implements Parser<String> {
  INSTANCE;

  @Override
  public String parse(String string) {
    return string;
  }
}
