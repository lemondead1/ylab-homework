package com.lemondead1.carshopservice.validation;

import com.lemondead1.carshopservice.exceptions.ValidationException;
import com.lemondead1.carshopservice.util.Util;
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
    Util.USERNAME.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "hello-54*&*3J._",
      "supercalifragilisticexpiolidocious"
  })
  void usernamePatternThrows(String testString) {
    assertThatThrownBy(() -> Util.USERNAME.validate(testString)).isInstanceOf(ValidationException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "password",
      "12345678 password"
  })
  void passwordPatternDoesNotThrow(String testString) {
    Util.PASSWORD.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "",
      "short"
  })
  void passwordPatternThrows(String testString) {
    assertThatThrownBy(() -> Util.PASSWORD.validate(testString)).isInstanceOf(ValidationException.class);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "+74378854353",
      "817352543554"
  })
  void phonePatternDoesNotThrow(String testString) {
    Util.PHONE_NUMBER.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "+7(522)132-64-74",
      "832434268548396834",
      "sometext"
  })
  void phonePatternThrows(String testString) {
    assertThatThrownBy(() -> Util.PHONE_NUMBER.validate(testString));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "test@example.com",
      "test@withsubdomain.mail.com",
      "ivan.ivanov@ya.ru"
  })
  void emailPatternDoesNotThrow(String testString) {
    Util.EMAIL.validate(testString);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "plaintext",
      "ip.address.email.is.spam.anyway@53.12.167.32"
  })
  void emailPatternThrows(String testString) {
    assertThatThrownBy(() -> Util.EMAIL.validate(testString));
  }
}
