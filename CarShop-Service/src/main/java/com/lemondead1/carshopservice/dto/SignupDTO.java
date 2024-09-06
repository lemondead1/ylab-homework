package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.util.Util;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Signup")
public record SignupDTO(@Schema(description = "The user's username.",
                                pattern = Util.USERNAME_REGEX,
                                example = "New_User") String username,
                        @Schema(description = "The user's phone number.",
                                pattern = Util.PHONE_NUMBER_REGEX,
                                example = "+71234567890") String phoneNumber,
                        @Schema(description = "The user's email.",
                                format = "email",
                                pattern = Util.EMAIL_REGEX,
                                example = "client@example.com") String email,
                        @Schema(description = "The user's password.",
                                format = "password",
                                pattern = Util.PASSWORD_REGEX,
                                example = "password") String password) { }
