package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;

public record NewOrderDTO(OrderKind kind,
                          OrderState state,
                          Integer clientId,
                          Integer carId,
                          String comment) { }
