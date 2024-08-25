package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.SignupDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.service.SignupLoginService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequiredArgsConstructor
public class SignupController {
  private final SignupLoginService signupLoginService;
  private final MapStruct mapStruct;

  @PostMapping("/signup")
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
