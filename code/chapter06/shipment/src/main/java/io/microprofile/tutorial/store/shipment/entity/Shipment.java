package io.microprofile.tutorial.store.shipment.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Shipment class for the microprofile tutorial store application.
 * This class represents a shipment of an order in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    private Long shipmentId;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;
    
    private String trackingNumber;
    
    @NotNull(message = "Status cannot be null")
    private ShipmentStatus status;
    
    private LocalDateTime estimatedDelivery;
    
    private LocalDateTime shippedAt;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    private String carrier;
    
    private String shippingAddress;
    
    private String notes;
}
