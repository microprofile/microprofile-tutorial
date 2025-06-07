package io.microprofile.tutorial.store.order.entity;

/**
 * OrderStatus enum for the microprofile tutorial store application.
 * This enum defines the possible statuses for an order.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
