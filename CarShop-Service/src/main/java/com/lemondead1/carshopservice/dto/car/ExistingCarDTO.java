package com.lemondead1.carshopservice.dto.car;

public record ExistingCarDTO(int id,
                             String brand,
                             String model,
                             Integer productionYear,
                             Integer price,
                             String condition,
                             boolean availableForPurchase) { }