package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.SignupDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.service.SignupLoginService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequestMapping(value = "/signup", consumes = "application/json", produces = "application/json")
@RequiredArgsConstructor
public class SignupController {
  private final SignupLoginService signupLoginService;
  private final MapStruct mapStruct;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Signs user up.", description = "Creates a new client user, ensuring username uniqueness.")
  @ApiResponse(responseCode = "201", description = "Signed up successfully.")
  @ApiResponse(responseCode = "409", description = "The given username was already taken.", content = @Content)
  ExistingUserDTO signup(@RequestBody SignupDTO signupDTO) {
    User newUser = signupLoginService.signUserUp(
        validate(signupDTO.username()).by(Util.USERNAME).nonnull("Username is required."),
        validate(signupDTO.phoneNumber()).by(Util.PHONE_NUMBER).nonnull("Phone number is required."),
        validate(signupDTO.email()).by(Util.EMAIL).nonnull("Email is required."),
        validate(signupDTO.password()).by(Util.PASSWORD).nonnull("Password is required.")
    );
    return mapStruct.userToUserDto(newUser);
  }
}
