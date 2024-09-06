package com.lemondead1.audit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Audited {
  /**
   * The type of the event.
   */
  String value();

  /**
   * Marks the parameter to be captured for audit.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface Param {
    /**
     * Key for the data map entry.
     */
    String value();
  }

  /**
   * Checks if argument is null and outputs true to audit if it is nonnull.
   */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.PARAMETER)
  @interface PresenceCheck {
    /**
     * Key for the data map entry.
     */
    String value();
  }
}
