package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.dto.user.NewUserDTO;
import com.lemondead1.carshopservice.dto.user.UserQueryDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final MapStruct mapStruct;

  @PostMapping("/users")
  @PreAuthorize("hasAuthority('admin')")
  ExistingUserDTO createUser(@RequestBody NewUserDTO userDTO) {
    User createdUser = userService.createUser(
        validate(userDTO.username()).by(Util.USERNAME).nonnull("Username is required."),
        validate(userDTO.phoneNumber()).by(Util.PHONE_NUMBER).nonnull("Phone number is required."),
        validate(userDTO.email()).by(Util.EMAIL).nonnull("Email is required."),
        validate(userDTO.password()).by(Util.PASSWORD).nonnull("Password is required."),
        validate(userDTO.role()).nonnull("Role is required.")
    );
    return mapStruct.userToUserDto(createdUser);
  }

  @GetMapping("/users/me")
  @PreAuthorize("isAuthenticated()")
  ExistingUserDTO showMe(@AuthenticationPrincipal User currentUser) {
    return mapStruct.userToUserDto(currentUser);
  }

  @PostMapping("/users/me")
  @PreAuthorize("isAuthenticated()")
  ExistingUserDTO editMe(@RequestBody NewUserDTO userDTO, @AuthenticationPrincipal User currentUser) {
    User editedUser = userService.editUser(
        currentUser.id(),
        validate(userDTO.username()).by(Util.USERNAME).orNull(),
        validate(userDTO.phoneNumber()).by(Util.PHONE_NUMBER).orNull(),
        validate(userDTO.email()).by(Util.EMAIL).orNull(),
        validate(userDTO.password()).by(Util.PASSWORD).orNull(),
        userDTO.role()
    );
    return mapStruct.userToUserDto(editedUser);
  }

  @GetMapping("/users/{userId}")
  @PreAuthorize("hasAuthority('client') and #userId == principal.id or hasAnyAuthority('manager', 'admin')")
  ExistingUserDTO findById(@PathVariable int userId) {
    User foundUser = userService.findById(userId);
    return mapStruct.userToUserDto(foundUser);
  }

  @PostMapping("/users/{userId}")
  @PreAuthorize("hasAuthority('admin')")
  ExistingUserDTO editById(@PathVariable int userId, @RequestBody NewUserDTO userDTO) {
    User editedUser = userService.editUser(
        userId,
        validate(userDTO.username()).by(Util.USERNAME).orNull(),
        validate(userDTO.phoneNumber()).by(Util.PHONE_NUMBER).orNull(),
        validate(userDTO.email()).by(Util.EMAIL).orNull(),
        validate(userDTO.password()).by(Util.PASSWORD).orNull(),
        userDTO.role()
    );
    return mapStruct.userToUserDto(editedUser);
  }

  @DeleteMapping
  @PreAuthorize("hasAuthority('admin')")
  void deleteUser(@PathVariable int userId, @RequestParam(required = false, defaultValue = "false") boolean cascade) {
    if (cascade) {
      userService.deleteUserCascading(userId);
    } else {
      userService.deleteUser(userId);
    }
  }

  @PostMapping("/users/search")
  @PreAuthorize("hasAnyAuthority('manager', 'admin')")
  List<ExistingUserDTO> searchUsers(@RequestBody UserQueryDTO queryDTO) {
    List<User> searchResult = userService.lookupUsers(
        coalesce(queryDTO.username(), ""),
        coalesce(queryDTO.roles(), UserRole.ALL),
        coalesce(queryDTO.phoneNumber(), ""),
        coalesce(queryDTO.email(), ""),
        coalesce(queryDTO.purchases(), Range.all()),
        coalesce(queryDTO.sorting(), UserSorting.USERNAME_ASC)
    );
    return mapStruct.userToUserDtoList(searchResult);
  }
}
