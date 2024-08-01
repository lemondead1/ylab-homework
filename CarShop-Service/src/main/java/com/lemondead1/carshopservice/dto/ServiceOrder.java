package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.PurchaseOrderState;

import java.time.Instant;

public record ServiceOrder(int id, Instant createdAt, PurchaseOrderState state, int customerId, int carId,
                           String complaints) { }
