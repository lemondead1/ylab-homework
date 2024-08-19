package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderState;

/**
 * The dto for editing orders.
 *
 * @param state New order state.
 * @param appendComment Comment line appended to the existing comment.
 */
public record EditOrderDTO(OrderState state, String appendComment) { }
