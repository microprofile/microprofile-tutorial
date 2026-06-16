package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * Payment service demonstrating MicroProfile Fault Tolerance patterns.
 * 
 * Each method demonstrates specific fault tolerance strategies:
 * - authorizePayment: Retry + Timeout + Fallback for transient failures
 * - checkGatewayHealth: Circuit Breaker + Timeout to prevent hammering failed services
 * - sendPaymentNotification: Asynchronous + Bulkhead + Fallback for resource isolation
 * 
 * These patterns work together to prevent cascading failures and ensure resilient payment processing.
 */
@ApplicationScoped
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    @ConfigProperty(name = "payment.gateway.endpoint", defaultValue = "https://api.paymentgateway.com")
    private String endpoint;

    /**
     * Authorize a payment transaction with fault tolerance.
     * 
     * Fault Tolerance Strategy:
     * - @Retry: Handles transient network failures (up to 3 retries with jitter)
     * - @Timeout: Prevents indefinite waits (3 second limit per attempt)
     * - @Fallback: Provides degraded service when gateway unavailable
     * 
     * @param paymentDetails Payment details to authorize
     * @return Authorization result JSON
     * @throws CriticalPaymentException For non-retryable failures (invalid card, insufficient funds)
     */
    @Retry(
        maxRetries = 3,
        delay = 2000,
        jitter = 500,
        retryOn = PaymentProcessingException.class,
        abortOn = CriticalPaymentException.class
    )
    @Timeout(3000)
    @Fallback(fallbackMethod = "fallbackAuthorizePayment")
    public String authorizePayment(PaymentDetails paymentDetails) 
        throws PaymentProcessingException {
        
        logger.info("Calling payment gateway at: " + endpoint);
        logger.info("Processing payment for amount: " + paymentDetails.getAmount());

        // Simulate network latency
        simulateDelay(1500);

        // Simulate transient failures (60% failure rate to demonstrate retries)
        if (Math.random() > 0.4) {
            throw new PaymentProcessingException(
                "Temporary payment gateway failure - will retry"
            );
        }

        return String.format(
            "{\"status\":\"success\",\"message\":\"Payment authorized\",\"amount\":%s}",
            paymentDetails.getAmount()
        );
    }

    /**
     * Fallback method for payment authorization.
     * Returns a pending status and queues payment for offline processing.
     * 
     * @param paymentDetails Payment details
     * @return Fallback response indicating payment is queued
     */
    public String fallbackAuthorizePayment(PaymentDetails paymentDetails) {
        logger.warning("Payment gateway unavailable - using fallback for amount: " 
            + paymentDetails.getAmount());
        
        // In production: queue for offline processing, use backup gateway, etc.
        return String.format(
            "{\"status\":\"pending\",\"message\":\"Payment queued for processing\",\"amount\":%s}",
            paymentDetails.getAmount()
        );
    }

    /**
     * Check payment gateway health with circuit breaker protection.
     * 
     * Fault Tolerance Strategy:
     * - @CircuitBreaker: Prevents hammering failed gateway (opens at 50% failure rate)
     * - @Timeout: Quick health check (2 second limit)
     * 
     * Circuit breaker will OPEN after 2 failures out of 4 requests (50% failure ratio),
     * wait 5 seconds in OPEN state, then transition to HALF_OPEN to test recovery.
     * Two consecutive successes in HALF_OPEN state will CLOSE the circuit.
     * 
     * @return true if gateway is healthy, false otherwise
     */
    @CircuitBreaker(
        failureRatio = 0.5,
        requestVolumeThreshold = 4,
        delay = 5000,
        successThreshold = 2
    )
    @Timeout(2000)
    public boolean checkGatewayHealth() {
        logger.info("Checking payment gateway health");
        
        // Simulate health check call
        simulateDelay(500);
        
        // Simulate intermittent gateway failures (60% success rate)
        if (Math.random() > 0.6) {
            throw new RuntimeException("Gateway health check failed");
        }
        
        return true;
    }

    /**
     * Send payment notification asynchronously with resource isolation.
     * 
     * Fault Tolerance Strategy:
     * - @Asynchronous: Non-blocking execution in separate thread
     * - @Bulkhead: Limits concurrent notifications (max 10 concurrent, queue up to 20)
     * - @Timeout: Prevents stuck notification threads (5 second limit)
     * - @Fallback: Logs failed notifications for later retry
     * 
     * Uses CompletionStage (not Future) to ensure fault tolerance annotations
     * react properly to asynchronous failures. With CompletionStage, exceptions
     * in exceptionally-completed stages trigger @Retry, @CircuitBreaker, and @Fallback.
     * 
     * @param paymentId Payment identifier
     * @param recipient Notification recipient email/phone
     * @return CompletionStage that completes when notification is sent
     */
    @Asynchronous
    @Bulkhead(value = 10, waitingTaskQueue = 20)
    @Timeout(5000)
    @Fallback(fallbackMethod = "fallbackSendNotification")
    public CompletionStage<String> sendPaymentNotification(
        String paymentId,
        String recipient
    ) {
        logger.info("Sending notification for payment: " + paymentId + " to " + recipient);

        // Simulate calling an external notification service (SMS/email gateway).
        // The delay keeps the bulkhead slot occupied so concurrent-load tests
        // can observe rejection when value + waitingTaskQueue is exceeded.
        simulateDelay(2000);

        // Simulate notification failures (80% success rate)
        if (Math.random() > 0.8) {
            logger.warning("Notification service unavailable for payment: " + paymentId);
            throw new RuntimeException("Notification service unavailable");
        }

        logger.info("Notification sent successfully for payment: " + paymentId);
        return CompletableFuture.completedFuture("Notification sent successfully");
    }

    /**
     * Fallback for notification - logs failure for later retry.
     * 
     * @param paymentId Payment identifier
     * @param recipient Notification recipient
     * @return CompletionStage indicating notification was queued
     */
    public CompletionStage<String> fallbackSendNotification(
        String paymentId, 
        String recipient
    ) {
        logger.warning("Failed to send notification for payment: " + paymentId);
        
        // In production: queue notification for retry, use backup channel (SMS if email failed), etc.
        return CompletableFuture.completedFuture(
            "Notification queued for retry"
        );
    }

    /**
     * Simulate network/processing delay.
     * 
     * @param milliseconds Delay duration in milliseconds
     */
    private void simulateDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}
