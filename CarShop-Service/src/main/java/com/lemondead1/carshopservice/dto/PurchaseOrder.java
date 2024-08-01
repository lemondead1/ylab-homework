package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.PurchaseOrderState;

import java.time.Instant;

public record PurchaseOrder(int id, Instant createdAt, PurchaseOrderState state, int customerId, int carId) { }
