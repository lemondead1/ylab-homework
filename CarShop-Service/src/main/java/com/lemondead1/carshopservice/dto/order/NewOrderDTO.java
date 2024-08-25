package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;

import javax.annotation.Nullable;

public record NewOrderDTO(@Nullable OrderKind kind,
                          @Nullable OrderState state,
                          @Nullable Integer clientId,
                          Integer carId,
                          @Nullable String comment) { }
