package com.lemondead1.carshopservice.cli.parsing;

import java.util.function.Function;

public interface Parser<T> {
  /**
   * Attempts to parse the given string
   *
   * @param string the string to be parsed
   * @return The parsed object
   * @throws com.lemondead1.carshopservice.exceptions.ParsingException on paring failure
   */
  T parse(String string);

  default <O> Parser<O> map(Function<T, O> function) {
    return string -> function.apply(parse(string));
  }
}
