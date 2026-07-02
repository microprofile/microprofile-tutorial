package io.microprofile.tutorial.store.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.logging.Logger;

/**
 * Reactive messaging handler that runs in the payment service.
 * Receives an order ID from order-created, authorizes payment,
 * and forwards the same order ID to payment-authorized.
 */
@ApplicationScoped
public class PaymentRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(PaymentRequestHandler.class.getName());

    @Incoming("order-created")
    @Outgoing("payment-authorized")
    public Long processPayment(Long orderId) {
        LOGGER.info(() -> "Processing payment for order " + orderId);
        // Payment authorization logic would go here
        return orderId;
    }
}
