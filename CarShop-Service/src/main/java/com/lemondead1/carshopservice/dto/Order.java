package com.lemondead1.carshopservice.dto;

import com.lemondead1.carshopservice.enums.OrderKind;
import com.lemondead1.carshopservice.enums.OrderState;

import java.time.Instant;

/**
 * Represents either a purchase or service order depending on {@code type}.
 *
 * @param id id
 * @param createdAt Creation timestamp
 * @param type kind
 * @param state current state
 * @param customer the customer for whom the order was created
 * @param car the car that is being purchased or performed service on
 * @param comments comments
 */
public record Order(int id, Instant createdAt, OrderKind type, OrderState state,
                    User customer, Car car, String comments) {
  public String prettyFormat() {
    var format = """
        %s order created at %s for customer %s for car %s with status %s with comments "%s" """;
    return String.format(format, type().getPrettyName(), createdAt(), customer().id(), car().id(),
                         state.getPrettyName(), comments());
  }
}
