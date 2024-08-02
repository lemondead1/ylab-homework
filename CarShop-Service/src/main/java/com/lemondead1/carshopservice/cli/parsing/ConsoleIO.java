package com.lemondead1.carshopservice.cli.parsing;

import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.exceptions.ValidationException;

import java.io.Console;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;

public class ConsoleIO {
  private final Console scanner = System.console();

  {
    if (scanner == null) {
      throw new IllegalStateException("No console was found.");
    }
  }

  public void println(String line) {
    System.out.println(line);
  }

  public void print(String string) {
    System.out.print(string);
  }

  public void printf(String pattern, Object... args) {
    System.out.printf(pattern, args);
  }

  public String readInteractive(String message) {
    System.out.print(message);
    return scanner.readLine();
  }

  public String readWhile(String message, Function<String, Optional<String>> feedback) {
    while (true) {
      var read = readInteractive(message);
      var fb = feedback.apply(read);
      if (fb.isEmpty()) {
        return read;
      } else {
        println(fb.get());
      }
    }
  }

  @SafeVarargs
  public final <T> T parse(String message, Parser<T> parser, Validator<T>... validators) {
    while (true) {
      var read = readInteractive(message);
      if (read.isEmpty()) {
        continue;
      }
      try {
        var parsed = parser.parse(read);
        for (var validator : validators) {
          validator.validate(parsed);
        }
        return parsed;
      } catch (ParsingException | ValidationException e) {
        println(e.getMessage());
      }
    }
  }

  @SafeVarargs
  public final <T> Optional<T> parseOptional(String message, Parser<T> parser, Validator<T>... validators) {
    while (true) {
      var read = readInteractive(message);
      if (read.isEmpty()) {
        return Optional.empty();
      }
      try {
        var parsed = parser.parse(read);
        for (var validator : validators) {
          validator.validate(parsed);
        }
        return Optional.of(parsed);
      } catch (ParsingException | ValidationException e) {
        println(e.getMessage());
      }
    }
  }
}
