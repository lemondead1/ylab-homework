package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.parsing.Parser;
import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;

import java.io.Console;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class ConsoleIO {
  private final Console scanner;
  private final Appendable out;

  public void println(String line) {
    try {
      out.append(line);
      out.append("\n");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void printf(String pattern, Object... args) {
    try {
      out.append(String.format(pattern, args));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String readInteractive(String message) {
    try {
      out.append(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
