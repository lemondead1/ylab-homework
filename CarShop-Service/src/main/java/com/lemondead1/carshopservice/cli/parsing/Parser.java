package com.lemondead1.carshopservice.cli.parsing;

public interface Parser<T> {
  T parse(String string);
}
