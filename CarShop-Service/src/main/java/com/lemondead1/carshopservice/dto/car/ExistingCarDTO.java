package com.lemondead1.carshopservice.dto.car;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "IdentifiedCar")
public record ExistingCarDTO(@Schema(description = "The car's id.")
                             int id,

                             @Schema(description = "The car's brand.", example = "Renault")
                             String brand,

                             @Schema(description = "The car's model.", example = "Logan")
                             String model,

                             @Schema(description = "The car's production year. Must not be greater than the current year.")
                             int productionYear,

                             @Schema(description = "The car's price.", example = "300000")
                             int price,

                             @Schema(description = "The car's condition.", example = "Like new")
                             String condition,

                             @Schema(description = "True if the car can be purchased.")
                             boolean availableForPurchase) { }