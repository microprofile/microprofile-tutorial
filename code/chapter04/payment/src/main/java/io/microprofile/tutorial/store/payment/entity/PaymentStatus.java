package io.microprofile.tutorial.store.payment.entity;

/**
 * PaymentStatus enum for the microprofile tutorial store application.
 * This enum defines the possible statuses for a payment.
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
}
