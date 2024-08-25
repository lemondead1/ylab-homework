package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.dto.user.NewUserDTO;
import com.lemondead1.carshopservice.dto.user.UserQueryDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequestMapping(value = "/users", consumes = "application/json", produces = "application/json")
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP)
@SecurityRequirement(name = "basicAuth")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final MapStruct mapStruct;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Creates a new user.",
             description = "Creates a new user, ensuring uniqueness of usernames. This operation is available for admins only.")
  @ApiResponse(responseCode = "201", description = "The user has been created successfully.")
  @ApiResponse(responseCode = "401", description = "The current user lacks sufficient permissions.", content = @Content)
  @ApiResponse(responseCode = "409", description = "The given username has already been taken.", content = @Content)
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

  @GetMapping("/me")
  @Operation(summary = "Returns the current user's data.")
  @ApiResponse(responseCode = "200", description = "Found current user's data.")
  ExistingUserDTO findCurrentUser(HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    return mapStruct.userToUserDto(currentUser);
  }

  @GetMapping("/{userId}")
  @Operation(summary = "Finds the user by id.",
             description = "Finds the user by id. Clients can only query their own profiles.")
  @ApiResponse(responseCode = "201", description = "The user was found successfully.")
  @ApiResponse(responseCode = "401", description = "The current user lacks sufficient permissions.", content = @Content)
  @ApiResponse(responseCode = "404", description = "Could not find the user by id.", content = @Content)
  ExistingUserDTO findUserById(@PathVariable int userId, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() == UserRole.CLIENT && currentUser.id() != userId) {
      throw new ForbiddenException("Clients cannot peek on other users' profiles.");
    }
    User foundUser = userService.findById(userId);
    return mapStruct.userToUserDto(foundUser);
  }

  @PatchMapping("/me")
  @Operation(summary = "Patches the current user.", description = "Patches the current user.")
  @ApiResponse
  ExistingUserDTO editCurrentUser(@RequestBody NewUserDTO userDTO, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() != UserRole.ADMIN && userDTO.role() != null) {
      throw new ForbiddenException("You cannot change roles.");
    }
    User editedUser = editUser(currentUser.id(), userDTO);
    return mapStruct.userToUserDto(editedUser);
  }

  @PatchMapping("/{userId}")
  ExistingUserDTO editUserBYId(@PathVariable int userId, @RequestBody NewUserDTO userDTO, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() != UserRole.ADMIN && userDTO.role() != null) {
      throw new ForbiddenException("You cannot change roles.");
    }
    User editedUser = editUser(userId, userDTO);
    return mapStruct.userToUserDto(editedUser);
  }

  private User editUser(int userId, NewUserDTO userDTO) {
    return userService.editUser(
        userId,
        validate(userDTO.username()).by(Util.USERNAME).orNull(),
        validate(userDTO.phoneNumber()).by(Util.PHONE_NUMBER).orNull(),
        validate(userDTO.email()).by(Util.EMAIL).orNull(),
        validate(userDTO.password()).by(Util.PASSWORD).orNull(),
        userDTO.role()
    );
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/me")
  void deleteCurrentUser(@RequestParam(defaultValue = "false") boolean cascade, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (cascade) {
      userService.deleteUserCascading(currentUser.id());
    } else {
      userService.deleteUser(currentUser.id());
    }
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{userId}")
  void deleteUserById(@PathVariable int userId, @RequestParam(defaultValue = "false") boolean cascade) {
    if (cascade) {
      userService.deleteUserCascading(userId);
    } else {
      userService.deleteUser(userId);
    }
  }

  @PostMapping("/search")
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
