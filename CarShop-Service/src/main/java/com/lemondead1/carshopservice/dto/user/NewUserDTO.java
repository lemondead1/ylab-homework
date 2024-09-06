package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;

public record NewUserDTO(String username,
                         String phoneNumber,
                         String email,
                         String password,
                         UserRole role) { }
