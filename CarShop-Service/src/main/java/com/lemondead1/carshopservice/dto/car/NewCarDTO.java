package com.lemondead1.carshopservice.dto.car;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * This dto is used as input where either the car does not exist yet or its id is known.
 */
@Schema(name = "Car")
public record NewCarDTO(@Schema(description = "The car's brand.", example = "Renault") String brand,
                        @Schema(description = "The car's model.", example = "Logan") String model,
                        @Schema(description = "The car's production year. Must not be greater than the current year.")
                        Integer productionYear,
                        @Schema(description = "The car's price.", example = "300000") Integer price,
                        @Schema(description = "The car's condition.", example = "Like new") String condition) { }
