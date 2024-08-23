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
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;
  private final MapStruct mapStruct;

  @PostMapping("/orders")
  @PreAuthorize("isAuthenticated()")
  ExistingOrderDTO createOrder(@RequestBody NewOrderDTO orderDTO, @AuthenticationPrincipal User currentUser) {
    var createdOrder = orderService.createOrder(
        coalesce(orderDTO.clientId(), currentUser.id()),
        validate(orderDTO.carId()).nonnull("Car id is required."),
        coalesce(orderDTO.kind(), OrderKind.PURCHASE),
        coalesce(orderDTO.state(), OrderState.NEW),
        coalesce(orderDTO.comment(), "")
    );
    return mapStruct.orderToOrderDto(createdOrder);
  }

  @PostMapping("/orders/{orderId}")
  @PreAuthorize("isAuthenticated()")
  ExistingOrderDTO editOrder(@PathVariable int orderId,
                             @RequestBody EditOrderDTO orderDTO,
                             @AuthenticationPrincipal User currentUser) {
    Order result = switch (currentUser.role()) {
      case CLIENT -> {
        var oldOrder = orderService.findById(orderId);
        if (oldOrder.client().id() != currentUser.id()) {
          throw new ForbiddenException("Clients cannot edit other users' orders.");
        }
        if (orderDTO.state() != null && orderDTO.state() != OrderState.CANCELLED) {
          throw new ForbiddenException("Clients can only cancel their orders.");
        }
        yield orderService.cancel(orderId, orderDTO.appendComment());
      }
      case MANAGER, ADMIN -> orderService.updateState(orderId, orderDTO.state(), orderDTO.appendComment());
    };
    return mapStruct.orderToOrderDto(result);
  }

  @GetMapping("/orders/{orderId}")
  @PreAuthorize("isAuthenticated()")
  ExistingOrderDTO findById(@PathVariable int orderId, @AuthenticationPrincipal User currentUser) {
    Order result = orderService.findById(orderId);
    if (currentUser.role() == UserRole.CLIENT && result.client().id() != currentUser.id()) {
      throw new ForbiddenException("Users cannot peek on other users' orders.");
    }
    return mapStruct.orderToOrderDto(result);
  }

  @DeleteMapping("/orders/{orderId}")
  @PreAuthorize("hasAuthority('admin')")
  void deleteOrder(@PathVariable int orderId) {
    orderService.deleteOrder(orderId);
  }

  @GetMapping("/users/me/orders")
  @PreAuthorize("isAuthenticated()")
  List<ExistingOrderDTO> findMyOrders(@RequestParam(defaultValue = "latest_first") OrderSorting sorting,
                                      @AuthenticationPrincipal(expression = "id") int userId) {
    return mapStruct.orderListToDtoList(orderService.findClientOrders(userId, sorting));
  }

  @GetMapping("/users/{userId}/orders")
  @PreAuthorize("hasAuthority('client') and #userId == principal.id or hasAnyAuthority('manager', 'admin')")
  List<ExistingOrderDTO> findUserOrders(@PathVariable int userId,
                                        @RequestParam(defaultValue = "latest_first") OrderSorting sorting) {
    return mapStruct.orderListToDtoList(orderService.findClientOrders(userId, sorting));
  }

  @PostMapping("/orders/search")
  @PreAuthorize("hasAnyAuthority('manager', 'admin')")
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
