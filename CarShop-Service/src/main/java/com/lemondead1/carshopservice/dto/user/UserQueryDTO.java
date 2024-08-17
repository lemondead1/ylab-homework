package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.util.Range;

import java.util.List;

public record UserQueryDTO(String username,
                           List<UserRole> roles,
                           String phoneNumber,
                           String email,
                           Range<Integer> purchases,
                           UserSorting sorting) { }
