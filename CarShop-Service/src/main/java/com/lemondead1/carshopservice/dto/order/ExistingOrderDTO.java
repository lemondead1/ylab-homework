package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "ExistingOrder")
public record ExistingOrderDTO(int id,
                               Instant createdAt,
                               OrderKind kind,
                               OrderState state,
                               ExistingUserDTO client,
                               ExistingCarDTO car,
                               String comment) { }
