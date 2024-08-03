package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.parsing.Parser;
import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;

import java.io.Console;
import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ConsoleIO {
  private final Console scanner;
  private final PrintStream out;

  public void println(String line) {
    out.println(line);
  }

  public void printf(String pattern, Object... args) {
    out.printf(pattern, args);
  }

  public String readInteractive(String message) {
    out.print(message);
    return scanner.readLine();
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
