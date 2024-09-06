package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.dto.car.ExistingCarDTO;
import com.lemondead1.carshopservice.dto.user.ExistingUserDTO;
import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;

import java.time.Instant;

public record ExistingOrderDTO(int id,
                               Instant createdAt,
                               OrderKind kind,
                               OrderState state,
                               ExistingUserDTO client,
                               ExistingCarDTO car,
                               String comment) { }
