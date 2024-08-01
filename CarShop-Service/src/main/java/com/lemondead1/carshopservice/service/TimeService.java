package com.lemondead1.carshopservice.service;

import java.time.Instant;

/**
 * Exists simply to mock Instant.now()
 */
public class TimeService {
  public Instant now() {
    return Instant.now();
  }
}
