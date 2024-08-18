package com.lemondead1.carshopservice.servlet.users;

import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.BadRequestException;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.exceptions.MethodNotAllowedException;
import com.lemondead1.carshopservice.exceptions.NotFoundException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.service.UserService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.lemondead1.carshopservice.ObjectMapperHolder.jackson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsersByIdServletTest extends ServletTest {
  @Mock
  UserService userService;

  @Mock
  OrderService orderService;

  UsersByIdServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new UsersByIdServlet(userService, orderService, mapStruct, jackson);
  }

  @Test
  @DisplayName("doGet on /users/100 calls findById and writes a user.")
  void testGetUser() throws IOException {
    var currentUser = new User(1, "admin", "80987654321", "admin@example.com", "password", UserRole.ADMIN, 0);
    var user = new User(100, "username", "81234567890", "user@example.com", "password", UserRole.CLIENT, 0);

    mockReqResp("/100", true, null, currentUser, Map.of());
    when(userService.findById(100)).thenReturn(user);

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    verify(userService).findById(100);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.userToUserDto(user)));
  }

  @Test
  @DisplayName("doGet on /users/100 throws when a client tries to peep on another user.")
  void doGetThrowsOnClientPeepingAttempt() throws IOException {
    var currentUser = new User(50, "admin", "80987654321", "user2@example.com", "password", UserRole.CLIENT, 0);

    mockReqResp("/100", false, null, currentUser, Map.of());

    assertThatThrownBy(() -> servlet.doGet(request, response)).isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("doGet on /users/me writes principal.")
  void testGetMe() throws IOException {
    var currentUser = new User(1, "admin", "80987654321", "admin@example.com", "password", UserRole.ADMIN, 0);

    mockReqResp("/me", true, null, currentUser, Map.of());

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.userToUserDto(currentUser)));
  }

  @Test
  @DisplayName("doGet on /users/me/orders?sorting=oldest_first calls findClientOrders and writes them.")
  void doGetOrdersWritesUserOrderList() throws IOException {
    var currentUser = new User(100, "username", "81234567890", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(10, "Renault", "Logan", 1999, 2000000, "mint", false);
    var orders = List.of(
        new Order(4, Instant.now(), OrderKind.PURCHASE, OrderState.DONE, currentUser, car, "None"),
        new Order(10, Instant.now(), OrderKind.SERVICE, OrderState.PERFORMING, currentUser, car, "Hit a tree")
    );

    when(orderService.findClientOrders(100, OrderSorting.CREATED_AT_ASC)).thenReturn(orders);
    mockReqResp("/me/orders", true, null, currentUser, Map.of("sorting", "oldest_first"));

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    verify(orderService).findClientOrders(100, OrderSorting.CREATED_AT_ASC);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderListToDtoList(orders)));
  }

  @Test
  @DisplayName("doPost on /users/100 calls editUser and writes the new user.")
  void doPostCallsEditUserAndWritesNew() throws IOException {
    var user = new User(100, "newUsername", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);

    var requestBody = "{\"username\": \"newUsername\", \"phone_number\": \"88005553535\"}";

    when(userService.editUser(user.id(), "newUsername", "88005553535", null, null, null)).thenReturn(user);
    mockReqResp("/100", true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(userService).editUser(user.id(), "newUsername", "88005553535", null, null, null);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.userToUserDto(user)));
  }

  @Test
  @DisplayName("doPost throws on /users/100/orders.")
  void doPostThrowsOnOrdersList() throws IOException {
    mockReqResp("/100/orders", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.doPost(request, response)).isInstanceOf(MethodNotAllowedException.class);
  }

  @Test
  @DisplayName("doDelete on /users/100 calls deleteUser.")
  void doDeleteCallsDeleteUser() throws IOException {
    mockReqResp("/100", false, null, null, Map.of());

    servlet.doDelete(request, response);

    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
    verify(userService).deleteUser(100);
  }

  @Test
  @DisplayName("doDelete on /users/100/orders throws.")
  void doDeleteThrowsOnOrdersList() throws IOException {
    mockReqResp("/100/orders", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.doDelete(request, response)).isInstanceOf(MethodNotAllowedException.class);
  }

  @Test
  @DisplayName("doDelete on /users/100?cascade=true calls deleteUserCascading.")
  void doDeleteCallsDeleteUserCascading() throws IOException {
    mockReqResp("/100", false, null, null, Map.of("cascade", "true"));

    servlet.doDelete(request, response);

    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
    verify(userService).deleteUserCascading(100);
  }

  @Test
  @DisplayName("parseQueryParams on /users/ throws.")
  void parseQueryParamsUnderflowTest() throws IOException {
    mockReqResp("/", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseQueryParams(request)).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("parseQueryParams on /users/100/something throws.")
  void parseQueryParamsMissingEndpointTest() throws IOException {
    mockReqResp("/100/something", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseQueryParams(request)).isInstanceOf(NotFoundException.class);
  }

  @Test
  @DisplayName("parseQueryParams on /notANumber throws.")
  void parseQueryParamsNaNTest() throws IOException {
    mockReqResp("/notANumber", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseQueryParams(request)).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("parseQueryParams on /users/100/orders/something throws.")
  void parseQueryParamsOverflowTest() throws IOException {
    mockReqResp("/100/orders/something", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseQueryParams(request)).isInstanceOf(NotFoundException.class);
  }
}
