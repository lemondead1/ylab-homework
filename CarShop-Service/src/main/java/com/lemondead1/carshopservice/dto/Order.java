package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.enums.OrderKind;

import java.time.Instant;

public record Order(int id, Instant createdAt, OrderKind type, OrderState state,
                    User customer, Car car, String comments) { }
