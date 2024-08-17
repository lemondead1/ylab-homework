package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.util.Range;

import java.time.Instant;
import java.util.List;

public record OrderQueryDTO(Range<Instant> dates,
                            String username,
                            String carBrand,
                            String carModel,
                            OrderKind kind,
                            List<OrderState> state,
                            OrderSorting sorting) { }
