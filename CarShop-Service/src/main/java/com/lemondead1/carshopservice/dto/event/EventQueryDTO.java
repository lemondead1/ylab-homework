package com.lemondead1.carshopservice.dto.event;

import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.Range;

import java.time.Instant;
import java.util.List;

public record EventQueryDTO(List<EventType> types, Range<Instant> dates, String username, EventSorting sorting) { }
