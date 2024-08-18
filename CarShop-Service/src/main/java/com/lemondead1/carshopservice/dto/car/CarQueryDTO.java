package com.lemondead1.carshopservice.dto.car;

import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;

public record CarQueryDTO(@Nullable String brand,
                          @Nullable String model,
                          @Nullable Range<Integer> productionYear,
                          @Nullable Range<Integer> price,
                          @Nullable String condition,
                          @Nullable Boolean availability,
                          @Nullable CarSorting sorting) { }
