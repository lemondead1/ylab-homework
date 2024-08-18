package com.lemondead1.carshopservice.servlet.users;

import com.lemondead1.carshopservice.dto.user.UserQueryDTO;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.enums.UserSorting;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.lemondead1.carshopservice.ObjectMapperHolder.jackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserSearchServletTest extends ServletTest {
  @Mock
  UserService userService;

  UserSearchServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new UserSearchServlet(userService, jackson, mapStruct);
  }

  @Test
  @DisplayName("doPost calls lookup and writes result.")
  void doPostCallsLookup() throws IOException {
    var query = new UserQueryDTO(null, List.of(UserRole.CLIENT), "12", "", null, UserSorting.USERNAME_ASC);
    var result = List.of(new User(12, "mike", "1234567890", "mike@example.com", "password", UserRole.CLIENT, 1),
                         new User(72, "nathan", "43284321264", "nathan@example.com", "password", UserRole.CLIENT, 2));

    var requestBody = jackson.writeValueAsString(query);

    when(userService.lookupUsers("", List.of(UserRole.CLIENT), "12", "", Range.all(), UserSorting.USERNAME_ASC)).thenReturn(result);
    mockReqResp(null, true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(userService).lookupUsers("", List.of(UserRole.CLIENT), "12", "", Range.all(), UserSorting.USERNAME_ASC);
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.userToUserDtoList(result)));
  }
}
