package com.lemondead1.carshopservice.servlet.orders;

import com.lemondead1.carshopservice.dto.order.NewOrderDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static com.lemondead1.carshopservice.SharedTestObjects.mapStruct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderCreationServletTest extends ServletTest {
  @Mock
  OrderService orderService;

  OrderCreationServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new OrderCreationServlet(orderService, mapStruct, jackson);
  }

  @Test
  @DisplayName("doPost calls createOrder and writes the result.")
  void doPostCallsCreateOrder() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(600, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, "Order");
    var newOrder = new NewOrderDTO(OrderKind.PURCHASE, null, 52, 100, "Order");

    var requestBody = jackson.writeValueAsString(newOrder);

    when(orderService.createOrder(52, 100, OrderKind.PURCHASE, OrderState.NEW, "Order")).thenReturn(order);
    mockReqResp(null, true, requestBody, user, Map.of());

    servlet.doPost(request, response);

    verify(response).setContentType("application/json");
    verify(response).setStatus(HttpStatus.CREATED_201);
    verify(orderService).createOrder(52, 100, OrderKind.PURCHASE, OrderState.NEW, "Order");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderToOrderDto(order)));
  }

  @Test
  @DisplayName("doPost throws when done by another client.")
  void doPostThrowsWhenDoneByAnotherUser() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var newOrder = new NewOrderDTO(OrderKind.PURCHASE, null, 100, 100, "Order");

    var requestBody = jackson.writeValueAsString(newOrder);

    mockReqResp(null, false, requestBody, user, Map.of());

    assertThatThrownBy(() -> servlet.doPost(request, response)).isInstanceOf(ForbiddenException.class);
  }

  @Test
  @DisplayName("doPost throws when done by a client and the status is not new.")
  void doPostThrowsWhenDoneByClientAndNotNew() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var newOrder = new NewOrderDTO(OrderKind.PURCHASE, OrderState.DONE, 52, 100, "Order");

    var requestBody = jackson.writeValueAsString(newOrder);

    mockReqResp(null, false, requestBody, user, Map.of());

    assertThatThrownBy(() -> servlet.doPost(request, response)).isInstanceOf(ForbiddenException.class);
  }
}
