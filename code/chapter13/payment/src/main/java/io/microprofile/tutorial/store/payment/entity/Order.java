package io.microprofile.tutorial.store.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Minimal order event payload handled by the payment service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long userId;
    private OrderStatus status = OrderStatus.CREATED;
}
