package com.lemondead1.carshopservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.order.OrderQueryDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {
  @MockBean
  OrderService orderService;

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  MapStruct mapStruct;

  @Autowired
  MockMvc mockMvc;

  @Test
  @DisplayName("POST /orders calls OrderService.createOrder and responds with the result.")
  void testCreateOrder() throws Exception {
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(8, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good");
    var requestBody = objectMapper.writeValueAsString(mapStruct.orderToNewOrderDto(order));
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderToOrderDto(order));

    when(orderService.createOrder(6, 7, OrderKind.PURCHASE, OrderState.NEW, "Good")).thenReturn(order);

    mockMvc.perform(post("/orders").principal(client)
                                   .content(requestBody).contentType("application/json")
                                   .accept("application/json"))
           .andDo(log())
           .andExpect(status().isCreated())
           .andExpect(content().string(expectedResponse));

    verify(orderService).createOrder(6, 7, OrderKind.PURCHASE, OrderState.NEW, "Good");
  }

  @Test
  @DisplayName("PATCH /orders/8 calls OrderService.updateState and responds with the edited order.")
  void testEditOrder() throws Exception {
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.MANAGER, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(8, Instant.now(), OrderKind.PURCHASE, OrderState.PERFORMING, client, car,
                          "Good\nArrival in 5 days.");
    var requestBody = "{\"state\": \"performing\", \"append_comment\": \"Arrival in 5 days.\"}";
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderToOrderDto(order));

    when(orderService.updateState(8, OrderState.PERFORMING, "Arrival in 5 days.")).thenReturn(order);

    mockMvc.perform(patch("/orders/8").principal(client)
                                      .content(requestBody).contentType("application/json")
                                      .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(orderService).updateState(8, OrderState.PERFORMING, "Arrival in 5 days.");
  }

  @Test
  @DisplayName("PATCH /orders/8 responds with FORBIDDEN if the user ids do not match and the principal is a client.")
  void testEditOrderThrowsForbiddenIfDifferentUserIdsAndPrincipalIsClient() throws Exception {
    var principal = new User(9, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);

    var client = new User(6, "user2", "88005553535", "user@example.com", "password", UserRole.MANAGER, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(8, Instant.now(), OrderKind.PURCHASE, OrderState.PERFORMING, client, car,
                          "Good\nArrival in 5 days.");
    var requestBody = "{\"state\": \"cancelled\"}";

    when(orderService.findById(8)).thenReturn(order);

    mockMvc.perform(patch("/orders/8").principal(principal)
                                      .content(requestBody).contentType("application/json")
                                      .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verify(orderService).findById(8);
  }

  @Test
  @DisplayName("PATCH /orders/8 responds with FORBIDDEN if the user ids do not match and the principal is a client.")
  void testEditOrderThrowsForbiddenIfStateIsNotCancelledAndPrincipalIsClient() throws Exception {
    var client = new User(6, "user2", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(8, Instant.now(), OrderKind.PURCHASE, OrderState.PERFORMING, client, car,
                          "Good\nArrival in 5 days.");
    var requestBody = "{\"state\": \"performing\", \"append_comment\": \"Arrival in 5 days.\"}";

    when(orderService.findById(8)).thenReturn(order);

    mockMvc.perform(patch("/orders/8").principal(client)
                                      .content(requestBody).contentType("application/json")
                                      .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verify(orderService).findById(8);
  }

  @Test
  @DisplayName("GET /orders/10 calls OrderService.findById and responds with the result.")
  void testFindOrderById() throws Exception {
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(10, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good");
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderToOrderDto(order));

    when(orderService.findById(10)).thenReturn(order);

    mockMvc.perform(get("/orders/10").principal(client).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(orderService).findById(10);
  }

  @Test
  @DisplayName("GET /orders/10 calls OrderService.findById and responds with the result.")
  void testFindOrderByIdThrowsForbiddenWhenUserIdsDontMatch() throws Exception {
    var principal = new User(100, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var order = new Order(10, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good");

    when(orderService.findById(10)).thenReturn(order);

    mockMvc.perform(get("/orders/10").principal(principal).contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verify(orderService).findById(10);
  }

  @Test
  @DisplayName("DELETE /orders/100 calls OrderService.deleteOrder and responds with the result.")
  void testDeleteOrderById() throws Exception {
    mockMvc.perform(delete("/orders/100").contentType("application/json").accept("application/json"))
           .andDo(log())
           .andExpect(status().isNoContent());

    verify(orderService).deleteOrder(100);
  }

  @Test
  @DisplayName("GET /users/me/orders calls OrderService.findClientOrders and responds with the result.")
  void testFindOrdersByCurrentUser() throws Exception {
    var client = new User(100, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var result = List.of(new Order(10, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good"));
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderListToDtoList(result));

    when(orderService.findClientOrders(100, OrderSorting.CAR_NAME_ASC)).thenReturn(result);

    mockMvc.perform(get("/users/me/orders?sorting=car_name").principal(client)
                                                            .contentType("application/json")
                                                            .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(orderService).findClientOrders(100, OrderSorting.CAR_NAME_ASC);
  }

  @Test
  @DisplayName("GET /users/6/orders calls OrderService.findClientOrders and responds with the result.")
  void testFindOrdersById() throws Exception {
    var principal = new User(100, "user", "88005553535", "user@example.com", "password", UserRole.MANAGER, 0);
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var result = List.of(new Order(10, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good"));
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderListToDtoList(result));

    when(orderService.findClientOrders(6, OrderSorting.CAR_NAME_ASC)).thenReturn(result);

    mockMvc.perform(get("/users/6/orders?sorting=car_name").principal(principal)
                                                           .contentType("application/json")
                                                           .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(orderService).findClientOrders(6, OrderSorting.CAR_NAME_ASC);
  }

  @Test
  @DisplayName("GET /users/6/orders responds with FORBIDDEN if the principal is a client and user ids don't match.")
  void testFindOrdersByIdThrowsForbiddenIfUserIdsDontMatch() throws Exception {
    var principal = new User(100, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);

    mockMvc.perform(get("/users/6/orders?sorting=car_name").principal(principal)
                                                           .contentType("application/json")
                                                           .accept("application/json"))
           .andDo(log())
           .andExpect(status().isForbidden());

    verifyNoInteractions(orderService);
  }

  @Test
  @DisplayName("POST /orders/search calls OrderService.lookupOrders and responds with the result.")
  void searchOrdersTest() throws Exception {
    var query = new OrderQueryDTO(null, null, "Hyu", "sol", OrderKind.PURCHASE, null, null);
    var client = new User(6, "user", "88005553535", "user@example.com", "password", UserRole.CLIENT, 0);
    var car = new Car(7, "Hyundai", "Solaris", 2010, 400000, "Poor", false);
    var result = List.of(new Order(10, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, client, car, "Good"));
    var requestBody = objectMapper.writeValueAsString(query);
    var expectedResponse = objectMapper.writeValueAsString(mapStruct.orderListToDtoList(result));

    when(orderService.lookupOrders(Range.all(),
                                   "",
                                   "Hyu",
                                   "sol",
                                   Set.of(OrderKind.PURCHASE),
                                   OrderState.ALL,
                                   OrderSorting.CREATED_AT_DESC)).thenReturn(result);

    mockMvc.perform(post("/orders/search").content(requestBody).contentType("application/json")
                                          .accept("application/json"))
           .andDo(log())
           .andExpect(status().isOk())
           .andExpect(content().string(expectedResponse));

    verify(orderService).lookupOrders(Range.all(),
                                      "",
                                      "Hyu",
                                      "sol",
                                      Set.of(OrderKind.PURCHASE),
                                      OrderState.ALL,
                                      OrderSorting.CREATED_AT_DESC);
  }
}
