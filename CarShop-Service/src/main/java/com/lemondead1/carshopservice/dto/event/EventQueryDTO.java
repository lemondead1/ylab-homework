package com.lemondead1.carshopservice.dto.event;

import com.lemondead1.carshopservice.enums.EventSorting;
import com.lemondead1.carshopservice.enums.EventType;
import com.lemondead1.carshopservice.util.Range;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;

/**
 * Represents a query accepted by the /events/search endpoint.
 *
 * @param types Allowed event types, filters none if null
 * @param dates Event timestamp range, filters none if null
 * @param username Username query
 * @param sorting Sorting
 */
@Schema(name = "EventQuery")
public record EventQueryDTO(@Nullable List<EventType> types,
                            @Nullable Range<Instant> dates,
                            @Nullable String username,
                            @Nullable EventSorting sorting) { }
