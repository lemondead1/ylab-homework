package com.lemondead1.carshopservice.dto.car;

import com.lemondead1.carshopservice.enums.CarSorting;
import com.lemondead1.carshopservice.util.Range;

public record CarQueryDTO(String brand,
                          String model,
                          Range<Integer> productionYear,
                          Range<Integer> price,
                          String condition,
                          Boolean availability,
                          CarSorting sorting) { }
