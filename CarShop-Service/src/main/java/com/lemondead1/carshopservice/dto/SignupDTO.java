package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.util.Util;

public record SignupDTO(String username, String phoneNumber, String email, String password) {
  public void validate() {
    Util.USERNAME.validate(username);
    Util.PHONE_NUMBER.validate(phoneNumber);
    Util.EMAIL.validate(email);
    Util.PASSWORD.validate(password);
  }
}
