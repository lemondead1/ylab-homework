package com.lemondead1.carshopservice.servlet.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemondead1.carshopservice.dto.order.OrderQueryDTO;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.service.OrderService;
import com.lemondead1.carshopservice.util.MapStruct;
import com.lemondead1.carshopservice.util.Range;
import com.lemondead1.carshopservice.util.Util;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

import static com.lemondead1.carshopservice.util.Util.coalesce;

@WebServlet(value = "/orders/search")
@ServletSecurity(@HttpConstraint(rolesAllowed = { "manager", "admin" }))
@RequiredArgsConstructor
public class OrderSearchServlet extends HttpServlet {
  private final OrderService orderService;
  private final MapStruct mapStruct;
  private final ObjectMapper objectMapper;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    var queryDto = objectMapper.readValue(req.getInputStream(), OrderQueryDTO.class);

    var result = orderService.lookupOrders(
        coalesce(queryDto.dates(), Range.all()),
        coalesce(queryDto.username(), ""),
        coalesce(queryDto.carBrand(), ""),
        coalesce(queryDto.carModel(), ""),
        queryDto.kind() == null ? OrderKind.ALL : List.of(queryDto.kind()),
        coalesce(queryDto.state(), OrderState.ALL),
        coalesce(queryDto.sorting(), OrderSorting.CREATED_AT_DESC)
    );

    resp.setContentType("application/json");
    var resultDto = mapStruct.orderListToDtoList(result);
    objectMapper.writeValue(resp.getOutputStream(),resultDto);
  }
}
