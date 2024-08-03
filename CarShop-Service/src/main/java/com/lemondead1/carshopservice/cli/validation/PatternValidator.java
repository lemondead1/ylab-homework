package com.lemondead1.carshopservice.cli.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;

import javax.annotation.RegEx;
import java.util.regex.Pattern;

public class PatternValidator implements Validator<String> {
  public static final Validator<String> USERNAME = new PatternValidator("[A-Za-z0-9_\\-.]{3,32}");
  public static final Validator<String> PASSWORD = new PatternValidator("[ -~]{8,}");
  public static final Validator<String> PHONE_NUMBER = new PatternValidator("+?\\d{8,13}");
  public static final Validator<String> EMAIL = new PatternValidator(".+@.{1,64}\\..{2,20}");

  private final Pattern pattern;

  public PatternValidator(@RegEx String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  @Override
  public void validate(String value) {
    if (!pattern.matcher(value).matches()) {
      throw new ValidationException("Input must match " + pattern + ".");
    }
  }
}
