package com.lemondead1.carshopservice.servlet.orders;

import com.lemondead1.carshopservice.dto.order.EditOrderDTO;
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

public class OrdersByIdServletTest extends ServletTest {
  @Mock
  OrderService orderService;

  OrdersByIdServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new OrdersByIdServlet(orderService, jackson, mapStruct);
  }

  @Test
  @DisplayName("doGet on /50 calls findById and writes the result.")
  void doGetCallsFindByIdAndWritesResult() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, "Order");

    when(orderService.findById(50)).thenReturn(order);
    mockReqResp("/50", true, null, user, Map.of());

    servlet.doGet(request, response);

    verify(response).setContentType("application/json");
    verify(orderService).findById(50);
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderToOrderDto(order)));
  }

  @Test
  @DisplayName("doGet on /50 calls findById throws when done by a different client.")
  void doGetCallsFindByIdAndThrows() throws IOException {
    var currentUser = new User(60, "username2", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, "Order");

    when(orderService.findById(50)).thenReturn(order);
    mockReqResp("/50", false, null, currentUser, Map.of());

    assertThatThrownBy(() -> servlet.doGet(request, response)).isInstanceOf(ForbiddenException.class);

    verify(orderService).findById(50);
  }

  @Test
  @DisplayName("doPost on /50 calls cancel when called by a client.")
  void doPostCallsCancelWhenDoneByClient() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var oldOrder = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, "Order");
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, user, car, "Order\nCancel");
    var dto = new EditOrderDTO(OrderState.CANCELLED, "Cancel");

    var requestBody = jackson.writeValueAsString(dto);

    when(orderService.findById(50)).thenReturn(oldOrder);
    when(orderService.cancel(50, "\nCancel")).thenReturn(order);
    mockReqResp("/50", true, requestBody, user, Map.of());

    servlet.doPost(request, response);

    verify(orderService).cancel(50, "\nCancel");
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderToOrderDto(order)));
  }

  @Test
  @DisplayName("doPost on /50 throws when done by a different client.")
  void doPostThrowsWhenDoneByAnotherUser() throws IOException {
    var currentUser = new User(60, "username2", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.NEW, user, car, "Order");
    var dto = new EditOrderDTO(OrderState.CANCELLED, "Cancel");

    var requestBody = jackson.writeValueAsString(dto);

    when(orderService.findById(50)).thenReturn(order);
    mockReqResp("/50", false, requestBody, currentUser, Map.of());

    assertThatThrownBy(() -> servlet.doPost(request, response)).isInstanceOf(ForbiddenException.class);

    verify(orderService).findById(50);
  }

  @Test
  @DisplayName("doPost on /50 throws when done by a client and does not cancel the order.")
  void doPostThrowsWhenDoneByClientAndNotCancel() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, user, car, "Order\nCancel");
    var dto = new EditOrderDTO(OrderState.DONE, "Cancel");

    var requestBody = jackson.writeValueAsString(dto);

    when(orderService.findById(50)).thenReturn(order);
    mockReqResp("/50", false, requestBody, user, Map.of());

    assertThatThrownBy(() -> servlet.doPost(request, response)).isInstanceOf(ForbiddenException.class);

    verify(orderService).findById(50);
  }

  @Test
  @DisplayName("doPost on /50 throws when done by a client and does not cancel the order.")
  void doPostCallsUpdateStateWhenDoneByAManager() throws IOException {
    var currentUser = new User(60, "username2", "88005553535", "test@example.com", "password", UserRole.MANAGER, 1);
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var order = new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.CANCELLED, user, car, "Order\nPerforming");
    var dto = new EditOrderDTO(OrderState.PERFORMING, "Performing");

    var requestBody = jackson.writeValueAsString(dto);

    when(orderService.updateState(50, OrderState.PERFORMING, "\nPerforming")).thenReturn(order);
    mockReqResp("/50", true, requestBody, currentUser, Map.of());

    servlet.doPost(request, response);

    verify(orderService).updateState(50, OrderState.PERFORMING, "\nPerforming");
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderToOrderDto(order)));
  }

  @Test
  @DisplayName("doDelete on /50 calls deleteOrder.")
  void doDeleteCallsDelete() throws IOException {
    mockReqResp("/50", false, null, null, Map.of());

    servlet.doDelete(request, response);

    verify(orderService).deleteOrder(50);
    verify(response).setStatus(HttpStatus.NO_CONTENT_204);
  }

  @Test
  @DisplayName("parseOrderId on / throws.")
  void parseOrderIdUnderflowTest() throws IOException {
    mockReqResp("/", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseOrderId(request)).isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("parseOrderId on /NaN throws.")
  void parseOrderIdNanTest() throws IOException {
    mockReqResp("/NaN", false, null, null, Map.of());

    assertThatThrownBy(() -> servlet.parseOrderId(request)).isInstanceOf(BadRequestException.class);
  }
}
