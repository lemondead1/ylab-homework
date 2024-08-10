package com.lemondead1.carshopservice.cli;

import com.lemondead1.carshopservice.cli.parsing.Parser;
import com.lemondead1.carshopservice.cli.validation.Validator;
import com.lemondead1.carshopservice.exceptions.ParsingException;
import com.lemondead1.carshopservice.exceptions.ValidationException;

import java.util.Optional;

public abstract class CLI {
  public static CLI getBestAvailable() {
    if (System.console() == null) {
      return new PlainCLI();
    } else {
      return new ConsoleCLI(System.console());
    }
  }

  public final void println(String line) {
    printf(line + "\n");
  }

  public abstract void printf(String pattern, Object... args);

  public abstract String readInteractive(String message);

  public abstract String readPassword(String message);

  @SafeVarargs
  public final <T> T parse(String message, Parser<T> parser, boolean hideInput, Validator<T>... validators) {
    while (true) {
      var read = hideInput ? readPassword(message) : readInteractive(message);
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
  public final <T> T parse(String message, Parser<T> parser, Validator<T>... validators) {
    return parse(message, parser, false, validators);
  }

  @SafeVarargs
  public final <T> Optional<T> parseOptional(String message, Parser<T> parser, boolean hideInput,
                                             Validator<T>... validators) {
    while (true) {
      var read = hideInput ? readPassword(message) : readInteractive(message);
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

  @SafeVarargs
  public final <T> Optional<T> parseOptional(String message, Parser<T> parser, Validator<T>... validators) {
    return parseOptional(message, parser, false, validators);
  }
}
