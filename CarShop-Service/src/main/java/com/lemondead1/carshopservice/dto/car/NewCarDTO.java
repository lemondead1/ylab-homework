package com.lemondead1.carshopservice.dto.car;

public record NewCarDTO(String brand,
                        String model,
                        Integer productionYear,
                        Integer price,
                        String condition) { }
