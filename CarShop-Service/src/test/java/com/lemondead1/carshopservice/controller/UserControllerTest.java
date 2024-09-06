package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.config.WebConfig;
import com.lemondead1.carshopservice.conversion.MapStruct;
import com.lemondead1.carshopservice.dto.user.UserQueryDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ComponentScan({ "com.lemondead1.carshopservice.conversion" })
@ContextConfiguration(classes = { WebConfig.class, UserController.class })
@AutoConfigureMockMvc
public class UserControllerTest {
  @MockBean
  UserService userService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  MapStruct mapStruct;

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("POST /users calls UserService.createUser and responds with the created user.")
  void testCreateUser() throws Exception {
    var u = new User(30, "SOmeUser", "+71234567890", "test@email.com", "password", UserRole.CLIENT, 0);
    var requestBody = objectMapper.writeValueAsString(mapStruct.userToNewUserDto(u));
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDto(u));

    when(userService.createUser(u.username(), u.phoneNumber(), u.email(), u.password(), u.role())).thenReturn(u);

    mockMvc.perform(post("/users").content(requestBody).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isCreated())
           .andExpect(content().string(expectedResponse));

    verify(userService).createUser(u.username(), u.phoneNumber(), u.email(), u.password(), u.role());
  }

  @Test
  @DisplayName("GET /users/me responds with current user principal.")
  void testFindCurrentUser() throws Exception {
    var u = new User(30, "SOmeUser", "+71234567890", "test@email.com", "password", UserRole.CLIENT, 0);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDto(u));

    mockMvc.perform(get("/users/me").principal(u).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));
  }

  @Test
  @DisplayName("GET /users/64 calls UserService.findById(64) and responds with the found user.")
  void testFindUserById() throws Exception {
    var principal = new User(1, "admin", "+71234567890", "admin@example.com", "password", UserRole.ADMIN, 0);
    var u = new User(64, "SOmeUser", "+71234567890", "test@email.com", "password", UserRole.CLIENT, 0);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDto(u));

    when(userService.findById(64)).thenReturn(u);

    mockMvc.perform(get("/users/64").principal(principal).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(userService).findById(64);
  }

  @Test
  @DisplayName("GET /users/64 responds with FORBIDDEN when the principal is client and its id is 1.")
  void testFindUserByIdThrowsForbiddenOnClientPeeping() throws Exception {
    var principal = new User(1, "client", "+71234567890", "client@example.com", "password", UserRole.CLIENT, 0);

    mockMvc.perform(get("/users/64").principal(principal).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verifyNoInteractions(userService);
  }

  @ParameterizedTest(name = "PATCH /users/{0} calls UserService.editUser and responds with the edited user.")
  @CsvSource({ "me, 43", "65, 65" })
  @DisplayName("Tests editUser* methods.")
  void testEditUser(String path, int id) throws Exception {
    var principal = new User(id, "client", "+71234567890", "client@example.com", "password", UserRole.CLIENT, 0);
    var editedUser = new User(id, "username", "88005553535", "client@example.com", "password", UserRole.CLIENT, 0);
    var requestBody = "{\"username\": \"username\", \"phone_number\": \"88005553535\"}";
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDto(editedUser));

    when(userService.editUser(id, "username", "88005553535", null, null, null)).thenReturn(editedUser);

    mockMvc.perform(patch("/users/{0}", path).principal(principal)
                                             .content(requestBody).contentType("application/json")
                                             .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(userService).editUser(id, "username", "88005553535", null, null, null);
  }

  @ParameterizedTest(name = "PATCH /users/{0} calls UserService.editUser and responds with the edited user.")
  @CsvSource({ "me, 47", "86, 86" })
  @DisplayName("Tests editUser* methods' permission checks.")
  void testEditCurrentUserForbiddenOnRoleAndClient(String path, int id) throws Exception {
    var principal = new User(id, "client", "+71234567890", "client@example.com", "password", UserRole.CLIENT, 0);
    var requestBody = "{\"role\": \"admin\"}";

    mockMvc.perform(patch("/users/{0}", path).principal(principal)
                                             .content(requestBody).contentType("application/json")
                                             .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verifyNoInteractions(userService);
  }

  @Test
  @DisplayName("PATCH /users/75 responds with FORBIDDEN when principal id is not 75 and its role is not admin.")
  void testEditUserByIdForbiddenOnDifferentUser() throws Exception {
    var principal = new User(85, "client", "+71234567890", "client@example.com", "password", UserRole.CLIENT, 0);
    var requestBody = "{\"username\": \"newUsername\"}";

    mockMvc.perform(patch("/users/75").principal(principal)
                                      .content(requestBody).contentType("application/json")
                                      .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verifyNoInteractions(userService);
  }

  @ParameterizedTest(name = "DELETE /users/{0}?cascade={1} calls UserService.{3}({2}).")
  @CsvSource({ "me, false, 5, deleteUser",
               "me, true, 5, deleteUserCascading",
               "43, false, 43, deleteUser",
               "26, true, 26, deleteUserCascading" })
  @DisplayName("Tests delete* methods.")
  void testDeleteUser(String path, boolean cascade, int id) throws Exception {
    var principal = new User(5, "client", "+71234567890", "client@example.com", "password", UserRole.ADMIN, 0);

    mockMvc.perform(delete("/users/{0}?cascade={1}", path, cascade).principal(principal)
                                                                   .contentType("application/json")
                                                                   .accept("application/json"))
           .andDo(log())
           .andExpect(status().isNoContent());

    if (cascade) {
      verify(userService).deleteUserCascading(id);
    } else {
      verify(userService).deleteUser(id);
    }
  }

  @Test
  @DisplayName("POST /users/search calls UserService.lookupUsers and responds with the result.")
  void testSearchUsers() throws Exception {
    var queryDto = new UserQueryDTO("abcdefg",
                                    List.of(UserRole.CLIENT),
                                    "12345",
                                    "hijklmn",
                                    new Range<>(2, null),
                                    UserSorting.USERNAME_ASC);
    var requestBody = objectMapper.writeValueAsString(queryDto);
    var found = List.of(new User(6, "abcdefghij", "8123456789", "hijklmn@example.com", "password", UserRole.CLIENT, 3));

    String expectedResponse = objectMapper.writeValueAsString(mapStruct.userToUserDtoList(found));

    when(userService.lookupUsers("abcdefg",
                                 List.of(UserRole.CLIENT),
                                 "12345",
                                 "hijklmn",
                                 new Range<>(2, null),
                                 UserSorting.USERNAME_ASC)).thenReturn(found);

    mockMvc.perform(post("/users/search").content(requestBody).contentType("application/json")
                                         .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(userService).lookupUsers("abcdefg",
                                    List.of(UserRole.CLIENT),
                                    "12345",
                                    "hijklmn",
                                    new Range<>(2, null),
                                    UserSorting.USERNAME_ASC);
  }
}
