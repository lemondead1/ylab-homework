package com.lemondead1.carshopservice.config;

import lombok.RequiredArgsConstructor;

import java.util.Properties;

@RequiredArgsConstructor
public class PropertiesConfig implements Config {
  private final Properties properties;

  @Override
  public String get(String key) {
    return properties.getProperty(key);
  }
}
