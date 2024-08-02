package com.lemondead1.carshopservice.util;

public class StringUtil {
  public static boolean containsIgnoreCase(String a, String b) {
    return b.isEmpty() || a.toLowerCase().contains(b.toLowerCase());
  }
}
