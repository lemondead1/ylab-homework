package com.lemondead1.carshopservice.dto.car;

/**
 * This dto is used as input where either the car does not exist yet or its id is known.
 */
public record NewCarDTO(String brand,
                        String model,
                        Integer productionYear,
                        Integer price,
                        String condition) { }
