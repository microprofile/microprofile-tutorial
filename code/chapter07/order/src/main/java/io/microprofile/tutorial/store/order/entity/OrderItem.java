package io.microprofile.tutorial.store.order.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * OrderItem class for the microprofile tutorial store application.
 * This class represents an item within an order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    private Long orderItemId;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Price at order cannot be null")
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private BigDecimal priceAtOrder;
}
