package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.UserRole;

public record User(int id, String username, String phoneNumber, String email, String password, UserRole role, int purchaseCount) { }
