package com.lemondead1.carshopservice.dto.event;

import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.Range;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

public record EventQueryDTO(@Nullable List<EventType> types,
                            @Nullable Range<Instant> dates,
                            @Nullable String username,
                            @Nullable EventSorting sorting) { }
