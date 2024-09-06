package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import java.util.List;

@Schema(name = "UserQuery")
public record UserQueryDTO(@Schema(description = "The username query.") @Nullable String username,
                           @Schema(description = "Allowed user roles.") @Nullable List<UserRole> roles,
                           @Schema(description = "Phone number query.") @Nullable String phoneNumber,
                           @Schema(description = "Email query.") @Nullable String email,
                           @Schema(description = "Purchase count range, inclusive.") @Nullable Range<Integer> purchases,
                           @Nullable UserSorting sorting) { }
