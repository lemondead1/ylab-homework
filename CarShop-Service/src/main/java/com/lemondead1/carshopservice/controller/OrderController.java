package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.dto.order.EditOrderDTO;
import com.lemondead1.carshopservice.dto.order.ExistingOrderDTO;
import com.lemondead1.carshopservice.dto.order.NewOrderDTO;
import com.lemondead1.carshopservice.dto.order.OrderQueryDTO;
import com.lemondead1.carshopservice.entity.Order;
import com.lemondead1.carshopservice.entity.User;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.UserRole;
import com.lemondead1.carshopservice.exceptions.ForbiddenException;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;
  private final MapStruct mapStruct;

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/orders")
  ExistingOrderDTO createOrder(@RequestBody NewOrderDTO orderDTO, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    var createdOrder = orderService.createOrder(
        coalesce(orderDTO.clientId(), currentUser.id()),
        validate(orderDTO.carId()).nonnull("Car id is required."),
        coalesce(orderDTO.kind(), OrderKind.PURCHASE),
        coalesce(orderDTO.state(), OrderState.NEW),
        coalesce(orderDTO.comment(), "")
    );
    return mapStruct.orderToOrderDto(createdOrder);
  }

  @PatchMapping("/orders/{orderId}")
  ExistingOrderDTO editOrderById(@PathVariable int orderId,
                                 @RequestBody EditOrderDTO orderDTO,
                                 HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() == UserRole.CLIENT) {
      var oldOrder = orderService.findById(orderId);
      if (oldOrder.client().id() != currentUser.id()) {
        throw new ForbiddenException("Clients cannot edit other users' orders.");
      }
      if (orderDTO.state() != null && orderDTO.state() != OrderState.CANCELLED) {
        throw new ForbiddenException("Clients can only cancel their orders.");
      }
    }
    Order result = orderService.updateState(orderId, orderDTO.state(), orderDTO.appendComment());
    return mapStruct.orderToOrderDto(result);
  }

  @GetMapping("/orders/{orderId}")
  ExistingOrderDTO findOrderById(@PathVariable int orderId, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    Order result = orderService.findById(orderId);
    if (currentUser.role() == UserRole.CLIENT && result.client().id() != currentUser.id()) {
      throw new ForbiddenException("Users cannot peek on other users' orders.");
    }
    return mapStruct.orderToOrderDto(result);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/orders/{orderId}")
  void deleteOrderById(@PathVariable int orderId) {
    orderService.deleteOrder(orderId);
  }

  @GetMapping("/users/me/orders")
  List<ExistingOrderDTO> findOrdersByCurrentUser(@RequestParam(defaultValue = "latest_first") OrderSorting sorting,
                                                 HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    return mapStruct.orderListToDtoList(orderService.findClientOrders(currentUser.id(), sorting));
  }

  @GetMapping("/users/{userId}/orders")
  List<ExistingOrderDTO> findOrdersByClientId(@PathVariable int userId,
                                              @RequestParam(defaultValue = "latest_first") OrderSorting sorting,
                                              HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() == UserRole.CLIENT && userId != currentUser.id()) {
      throw new ForbiddenException("Clients cannot view other users' orders.");
    }
    return mapStruct.orderListToDtoList(orderService.findClientOrders(userId, sorting));
  }

  @PostMapping("/orders/search")
  List<ExistingOrderDTO> searchOrders(@RequestBody OrderQueryDTO queryDTO) {
    List<Order> found = orderService.lookupOrders(
        coalesce(queryDTO.dates(), Range.all()),
        coalesce(queryDTO.username(), ""),
        coalesce(queryDTO.carBrand(), ""),
        coalesce(queryDTO.carModel(), ""),
        Optional.ofNullable(queryDTO.kind()).map(Set::of).orElse(EnumSet.allOf(OrderKind.class)),
        coalesce(queryDTO.state(), OrderState.ALL),
        coalesce(queryDTO.sorting(), OrderSorting.CREATED_AT_DESC)
    );
    return mapStruct.orderListToDtoList(found);
  }
}
