package com.lemondead1.carshopservice.dto.car;

import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;

/**
 * Represents a query sent to the /cars/search endpoint.
 *
 * @param brand car brand query
 * @param model car model query
 * @param productionYear car production year range
 * @param price car price range
 * @param condition car condition query
 * @param availability car availability, matches any if {@code null}
 * @param sorting entry sorting
 */
@Schema(name = "CarQuery")
public record CarQueryDTO(@Nullable String brand,
                          @Nullable String model,
                          @Nullable Range<Integer> productionYear,
                          @Nullable Range<Integer> price,
                          @Nullable String condition,
                          @Nullable Boolean availability,
                          @Nullable CarSorting sorting) { }
