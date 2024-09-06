package com.lemondead1.carshopservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.MapStructImpl;

public class SharedTestObjects {
  public static final ObjectMapper jackson = CarShopServiceApplication.createObjectMapper();
  public static final MapStruct mapStruct = new MapStructImpl();
}
