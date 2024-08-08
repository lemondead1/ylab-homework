package com.lemondead1.carshopservice.config;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MapConfig implements Config {
  private final Map<String, String> map;

  @Override
  public String get(String key) {
    return map.get(key);
  }
}
