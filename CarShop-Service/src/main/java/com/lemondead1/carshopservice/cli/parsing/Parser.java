package com.lemondead1.carshopservice.cli.parsing;

import java.util.function.Function;

public interface Parser<T> {
  T parse(String string);

  default <O> Parser<O> map(Function<T, O> function) {
    return string -> function.apply(parse(string));
  }
}
