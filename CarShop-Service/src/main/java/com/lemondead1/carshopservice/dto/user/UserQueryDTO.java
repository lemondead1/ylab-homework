package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.util.List;

public record UserQueryDTO(@Nullable String username,
                           @Nullable List<UserRole> roles,
                           @Nullable String phoneNumber,
                           @Nullable String email,
                           @Nullable Range<Integer> purchases,
                           @Nullable UserSorting sorting) { }
