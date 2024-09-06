package com.lemondead1.carshopservice.controller;

import com.lemondead1.carshopservice.conversion.MapStruct;
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
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lemondead1.carshopservice.util.Util.coalesce;
import static com.lemondead1.carshopservice.validation.Validated.validate;

@RestController
@RequestMapping(consumes = "application/json", produces = "application/json")
@RequiredArgsConstructor
public class OrderController {
  private final OrderService orderService;
  private final MapStruct mapStruct;

  @PostMapping("/orders")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Places a new order.", description = "Places a new order. Clients can only place orders for themselves.")
  @ApiResponse(responseCode = "201", description = "The order was created successfully.")
  @ApiResponse(responseCode = "409", description = "Either the car is not available for purchase or the client does not own the car (for service orders).", content = @Content)
  ExistingOrderDTO createOrder(@RequestBody NewOrderDTO orderDTO, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    if (currentUser.role() == UserRole.CLIENT && orderDTO.clientId() != null && orderDTO.clientId() != currentUser.id()) {
      throw new ForbiddenException("You cannot create orders for other users.");
    }
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
  @Operation(summary = "Modifies an order by id.", description = "Modifies an order by id. Clients are only allowed to cancel and leave comments under their own orders.")
  @ApiResponse(responseCode = "200", description = "The order was updated successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find an order by the given id.", content = @Content)
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
  @Operation(summary = "Finds an order by id.", description = "Finds an order by id. Clients are only allowed to view their own orders.")
  @ApiResponse(responseCode = "200", description = "The order was found successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find an order by the given id.", content = @Content)
  ExistingOrderDTO findOrderById(@PathVariable int orderId, HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    Order result = orderService.findById(orderId);
    if (currentUser.role() == UserRole.CLIENT && result.client().id() != currentUser.id()) {
      throw new ForbiddenException("Users cannot peek on other users' orders.");
    }
    return mapStruct.orderToOrderDto(result);
  }

  @DeleteMapping("/orders/{orderId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Deletes an order by id.", description = "Deletes an order by id. Only allowed for admins.")
  @ApiResponse(responseCode = "204", description = "The order was deleted successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find an order by the given id.", content = @Content)
  @ApiResponse(responseCode = "409", description = "There exist service orders that depend on this purchase.", content = @Content)
  void deleteOrderById(@PathVariable int orderId) {
    orderService.deleteOrder(orderId);
  }

  @GetMapping("/users/me/orders")
  @Operation(summary = "Finds orders by the current user.", description = "Finds orders by the current user.")
  @ApiResponse(responseCode = "200", description = "Found the current user's orders successfully.")
  List<ExistingOrderDTO> findOrdersByCurrentUser(@RequestParam(defaultValue = "latest_first") OrderSorting sorting,
                                                 HttpServletRequest request) {
    User currentUser = (User) request.getUserPrincipal();
    return mapStruct.orderListToDtoList(orderService.findClientOrders(currentUser.id(), sorting));
  }

  @GetMapping("/users/{userId}/orders")
  @Operation(summary = "Finds orders by client id.", description = "Finds orders by client id. Clients are only allowed to view their own orders.")
  @ApiResponse(responseCode = "200", description = "Found the user's orders successfully.")
  @ApiResponse(responseCode = "404", description = "Could not find a user with the given id.", content = @Content)
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
  @Operation(summary = "Searches for orders matching query.", description = "Searches for orders matching query. Not allowed for clients.")
  @ApiResponse(responseCode = "200", description = "Search completed successfully.")
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
