package com.lemondead1.carshopservice;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperHolder {
  public static final ObjectMapper jackson = CarShopServiceApplication.createObjectMapper();
}
