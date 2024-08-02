package com.lemondead1.carshopservice.util;

public class StringUtil {
  public static boolean containsIgnoreCase(String a, String b) {
    return a.toLowerCase().contains(b.toLowerCase());
  }
}
