package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderSorting;
import com.lemondead1.carshopservice.enums.OrderState;
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

@Schema(name = "OrderQuery")
public record OrderQueryDTO(@Nullable Range<Instant> dates,
                            @Nullable String username,
                            @Nullable String carBrand,
                            @Nullable String carModel,
                            @Nullable OrderKind kind,
                            @Nullable List<OrderState> state,
                            @Nullable OrderSorting sorting) { }
