package io.microprofile.tutorial.store.payment.entity;

/**
 * Basic lifecycle states recognized by the payment service.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    CANCELLED
}
