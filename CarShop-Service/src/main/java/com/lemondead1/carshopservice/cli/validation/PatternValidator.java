package com.lemondead1.carshopservice.cli.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import org.intellij.lang.annotations.Language;

import java.util.regex.Pattern;

public class PatternValidator implements Validator<String> {
  private final Pattern pattern;

  public PatternValidator(@Language("REGEXP") String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  @Override
  public void validate(String value) {
    if (!pattern.matcher(value).matches()) {
      throw new ValidationException("Input must match " + pattern + ".");
    }
  }
}
