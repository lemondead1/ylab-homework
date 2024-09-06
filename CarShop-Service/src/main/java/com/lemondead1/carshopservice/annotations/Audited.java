package com.lemondead1.carshopservice.annotations;

import com.lemondead1.carshopservice.enums.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {
  EventType value();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface Param {
    String value();
  }

  /**
   * Checks if argument is null and outputs true to audit if it is nonnull.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface PresenceCheck {
    String value();
  }
}
