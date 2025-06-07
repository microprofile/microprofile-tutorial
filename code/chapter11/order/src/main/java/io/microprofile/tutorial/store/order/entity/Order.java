package io.microprofile.tutorial.store.order.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order class for the microprofile tutorial store application.
 * This class represents an order in the system with its details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    private Long orderId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Total price cannot be null")
    @Min(value = 0, message = "Total price must be greater than or equal to 0")
    private BigDecimal totalPrice;

    @NotNull(message = "Status cannot be null")
    private OrderStatus status;

    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
}
