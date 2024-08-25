package com.lemondead1.carshopservice.util;

import com.fasterxml.jackson.annotation.JsonValue;

public interface HasId {
  @JsonValue
  String getId();
}
