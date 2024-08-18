package com.lemondead1.carshopservice.servlet.orders;

import com.lemondead1.carshopservice.dto.order.OrderQueryDTO;
import com.lemondead1.carshopservice.entity.Car;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.servlet.ServletTest;
import com.lemondead1.carshopservice.util.Range;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.lemondead1.carshopservice.SharedTestObjects.jackson;
import static com.lemondead1.carshopservice.SharedTestObjects.mapStruct;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderSearchServletTest extends ServletTest {
  @Mock
  OrderService orderService;

  OrderSearchServlet servlet;

  @BeforeEach
  void beforeEach() {
    servlet = new OrderSearchServlet(orderService, mapStruct, jackson);
  }

  @Test
  @DisplayName("doPost calls lookupOrders and writes the result.")
  void doPostCallsLookupAndWritesResult() throws IOException {
    var user = new User(52, "username", "88005553535", "test@example.com", "password", UserRole.CLIENT, 1);
    var car = new Car(100, "Alfa Romeo", "164", 1992, 1500000, "good", true);
    var result = List.of(new Order(50, Instant.now(), OrderKind.PURCHASE, OrderState.DONE, user, car, "Done"),
                         new Order(173, Instant.now(), OrderKind.SERVICE, OrderState.PERFORMING, user, car, ""));
    var query = new OrderQueryDTO(Range.all(), "userna", null, null, null, null, OrderSorting.CREATED_AT_ASC);

    var requestBody = jackson.writeValueAsString(query);

    when(orderService.lookupOrders(Range.all(), "userna", "", "", OrderKind.ALL,
                                   OrderState.ALL, OrderSorting.CREATED_AT_ASC))
        .thenReturn(result);
    mockReqResp(null, true, requestBody, null, Map.of());

    servlet.doPost(request, response);

    verify(orderService).lookupOrders(Range.all(), "userna", "", "", OrderKind.ALL,
                                      OrderState.ALL, OrderSorting.CREATED_AT_ASC);
    verify(response).setContentType("application/json");
    assertThat(responseBody.toString()).isEqualTo(jackson.writeValueAsString(mapStruct.orderListToDtoList(result)));
  }
}
