package com.lemondead1.carshopservice.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Exists simply to mock Instant.now()
 */
@Service
public class TimeService {
  public Instant now() {
    return Instant.now();
  }
}
