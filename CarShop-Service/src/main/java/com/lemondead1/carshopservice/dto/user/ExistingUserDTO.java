package com.lemondead1.carshopservice.dto.user;

import com.lemondead1.carshopservice.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IdentifiedUser")
public record ExistingUserDTO(@Schema(description = "The user's id.") int id,
                              @Schema(description = "The user's username.") String username,
                              @Schema(description = "The user's phone number.") String phoneNumber,
                              @Schema(description = "The user's email.") String email,
                              @Schema(description = "The user's role.") UserRole role,
                              @Schema(description = "The user's purchase count.") int purchaseCount) { }
