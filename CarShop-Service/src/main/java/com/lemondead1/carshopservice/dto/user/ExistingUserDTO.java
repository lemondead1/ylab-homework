package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;

public record ExistingUserDTO(int id,
                              String username,
                              String phoneNumber,
                              String email,
                              UserRole role,
                              int purchaseCount) { }
