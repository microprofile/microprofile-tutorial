package io.microprofile.tutorial.store.shipment.entity;

/**
 * ShipmentStatus enum for the microprofile tutorial store application.
 * This enum defines the possible statuses for a shipment.
 */
public enum ShipmentStatus {
    PENDING,         // Shipment is pending
    PROCESSING,      // Shipment is being processed 
    SHIPPED,         // Shipment has been shipped
    IN_TRANSIT,      // Shipment is in transit
    OUT_FOR_DELIVERY,// Shipment is out for delivery
    DELIVERED,       // Shipment has been delivered
    FAILED,          // Shipment delivery failed
    RETURNED         // Shipment was returned
}
