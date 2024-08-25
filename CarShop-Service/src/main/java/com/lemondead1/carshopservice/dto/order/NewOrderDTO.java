package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

@Schema(name = "NewOrder")
public record NewOrderDTO(@Nullable OrderKind kind,
                          @Nullable OrderState state,
                          @Nullable Integer clientId,
                          Integer carId,
                          @Nullable String comment) { }
