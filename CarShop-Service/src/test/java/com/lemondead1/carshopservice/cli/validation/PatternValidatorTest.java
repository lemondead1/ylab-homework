package com.lemondead1.carshopservice.cli.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PatternValidatorTest {
  @ParameterizedTest
  @ValueSource(strings = {
      "abcdefh",
      "hello-543J._"
  })
  void usernamePatternDoesNotThrow(String testString) {
    PatternValidator.USERNAME.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "hello-54*&*3J._",
      "supercalifragilisticexpiolidocious"
  })
  void usernamePatternThrows(String testString) {
    assertThatThrownBy(() -> PatternValidator.USERNAME.validate(testString)).isInstanceOf(ValidationException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "password",
      "12345678 password"
  })
  void passwordPatternDoesNotThrow(String testString) {
    PatternValidator.PASSWORD.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "short"
  })
  void passwordPatternThrows(String testString) {
    assertThatThrownBy(() -> PatternValidator.PASSWORD.validate(testString)).isInstanceOf(ValidationException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "+74378854353",
      "817352543554"
  })
  void phonePatternDoesNotThrow(String testString) {
    PatternValidator.PHONE_NUMBER.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "+7(522)132-64-74",
      "832434268548396834",
      "sometext"
  })
  void phonePatternThrows(String testString) {
    assertThatThrownBy(() -> PatternValidator.PHONE_NUMBER.validate(testString));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "test@example.com",
      "test@withsubdomain.mail.com",
      "ivan.ivanov@ya.ru"
  })
  void emailPatternDoesNotThrow(String testString) {
    PatternValidator.EMAIL.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "plaintext",
      "ip.address.email.is.spam.anyway@53.12.167.32"
  })
  void emailPatternThrows(String testString) {
    assertThatThrownBy(() -> PatternValidator.EMAIL.validate(testString));
  }
}
