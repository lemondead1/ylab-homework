package com.lemondead1.carshopservice.dto.car;

public record ExistingCarDTO(int id,
                             String brand,
                             String model,
                             int productionYear,
                             int price,
                             String condition,
                             boolean availableForPurchase) { }