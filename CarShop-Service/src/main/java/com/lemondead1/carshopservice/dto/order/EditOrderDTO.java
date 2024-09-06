package com.lemondead1.carshopservice.dto.order;

import com.lemondead1.carshopservice.enums.OrderState;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * The dto for editing orders.
 *
 * @param state New order state.
 * @param appendComment Comment line appended to the existing comment.
 */
@Schema(name = "EditOrder")
public record EditOrderDTO(OrderState state, String appendComment) { }
