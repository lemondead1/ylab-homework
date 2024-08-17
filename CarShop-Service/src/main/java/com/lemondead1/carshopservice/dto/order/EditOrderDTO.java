package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderState;

public record EditOrderDTO(OrderState state, String appendComment) { }
