package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;
import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    @Inject
    Tracer tracer;

    @Inject
    Meter meter;

    private LongCounter paymentAttemptsCounter;

    @PostConstruct
    public void init() {
        paymentAttemptsCounter = meter
                .counterBuilder("payment.attempts")
                .setDescription("Number of payment attempts by result")
                .setUnit("1")
                .build();
        logger.info("PaymentService initialized with telemetry instrumentation");
    }

    @Asynchronous
    @Timeout(3000)
    @Retry(maxRetries = 3, delay = 2000, jitter = 500, retryOn = PaymentProcessingException.class, abortOn = CriticalPaymentException.class)
    @Fallback(fallbackMethod = "fallbackProcessPayment")
    @Bulkhead(value = 5)
    public CompletionStage<String> processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException {
        Span span = tracer.spanBuilder("payment.process")
                .setAttribute("payment.amount", paymentDetails.getAmount().toString())
                .setAttribute("payment.method", "credit_card")
                .setAttribute("payment.service", "payment-service")
                .startSpan();

        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            String maskedCardNumber = maskCardNumber(paymentDetails.getCardNumber());
            logger.info(String.format("Processing payment - Amount: %s, Card: %s",
                    paymentDetails.getAmount(), maskedCardNumber));

            span.setAttribute("payment.status", "IN_PROGRESS");
            span.addEvent("Starting payment processing");
            simulateDelay();

            if (Math.random() > 0.7) {
                paymentAttemptsCounter.add(1, Attributes.of(AttributeKey.stringKey("result"), "failed"));
                span.setStatus(StatusCode.ERROR, "Payment processing failed");
                span.addEvent("Payment processing failed due to transient error");
                logger.warning("Payment processing failed due to transient error");
                throw new PaymentProcessingException("Temporary payment processing failure");
            }

            paymentAttemptsCounter.add(1, Attributes.of(AttributeKey.stringKey("result"), "success"));
            span.setAttribute("payment.status", "SUCCESS");
            span.setStatus(StatusCode.OK);
            span.addEvent("Payment processed successfully");
            logger.info("Payment processed successfully");
            return CompletableFuture.completedFuture("{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}");
        } finally {
            span.end();
        }
    }

    public CompletionStage<String> fallbackProcessPayment(PaymentDetails paymentDetails) {
        paymentAttemptsCounter.add(1, Attributes.of(AttributeKey.stringKey("result"), "fallback"));
        logger.warning(() -> String.format("Fallback invoked for payment - Amount: %s",
                paymentDetails.getAmount()));
        return CompletableFuture.completedFuture("{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
    }

    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.75, delay = 1000, successThreshold = 2)
    public boolean checkGatewayHealth() {
        logger.info("Checking payment gateway health");
        simulateNetworkCall(200);
        if (Math.random() > 0.9) {
            logger.warning("Gateway health check failed");
            throw new RuntimeException("Payment gateway not responding");
        }
        logger.info("Gateway is healthy");
        return true;
    }

    @Asynchronous
    @Bulkhead(5)
    public CompletionStage<String> sendPaymentNotification(String paymentId, String recipient) {
        logger.info(() -> String.format("Sending payment notification - Payment ID: %s, Recipient: %s",
                paymentId, recipient));
        simulateNetworkCall(300);
        logger.info("Payment notification sent successfully");
        return CompletableFuture.completedFuture(
                String.format("Notification sent to %s for payment %s", recipient, paymentId));
    }

    @Asynchronous
    public CompletionStage<String> verifyPaymentWithTelemetry(PaymentDetails paymentDetails, String transactionId)
            throws PaymentProcessingException {
        logger.info(() -> String.format("Starting payment verification - Transaction ID: %s", transactionId));

        try {
            validatePaymentDetails(paymentDetails);
            performFraudCheck(paymentDetails, transactionId);
            verifyFundsAvailability(paymentDetails);
            recordTransaction(paymentDetails, transactionId);

            logger.info("Payment verification completed successfully");
            return CompletableFuture.completedFuture(
                    String.format("{\"status\":\"verified\", \"transaction_id\":\"%s\", \"message\":\"Payment verification complete.\"}",
                            transactionId));
        } catch (Exception e) {
            logger.severe(() -> String.format("Payment verification failed: %s", e.getMessage()));
            throw e;
        }
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "INVALID_CARD";
        }
        int length = cardNumber.length();
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < length - 4; i++) {
            masked.append('X');
        }
        masked.append(cardNumber.substring(length - 4));
        return masked.toString();
    }

    private void simulateDelay() {
        try {
            logger.fine("Starting payment processing delay simulation");
            Thread.sleep(1500);
            logger.fine("Payment processing delay simulation completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Payment processing interrupted");
            throw new RuntimeException("Processing interrupted");
        }
    }

    private void validatePaymentDetails(PaymentDetails details) throws PaymentProcessingException {
        logger.info("Validating payment details");
        boolean isValid = details.getCardNumber() != null &&
                details.getCardNumber().length() >= 15 &&
                details.getExpiryDate() != null &&
                details.getAmount() != null &&
                details.getAmount().doubleValue() > 0;
        if (!isValid) {
            logger.warning("Payment details validation failed");
            throw new PaymentProcessingException("Payment details validation failed");
        }
        logger.info("Payment details validation successful");
    }

    private void performFraudCheck(PaymentDetails details, String transactionId) throws PaymentProcessingException {
        logger.info(() -> String.format("Performing fraud check for transaction: %s", transactionId));
        simulateNetworkCall(300);
        boolean isSafe = !details.getCardNumber().endsWith("0000");
        if (!isSafe) {
            logger.warning("Potential fraud detected");
            throw new PaymentProcessingException("Fraud check failed");
        }
        logger.info("Fraud check passed");
    }

    private void verifyFundsAvailability(PaymentDetails details) throws PaymentProcessingException {
        logger.info(() -> String.format("Verifying funds availability - Amount: %s", details.getAmount()));
        simulateNetworkCall(500);
        boolean hasFunds = details.getAmount().doubleValue() <= 1000;
        if (!hasFunds) {
            logger.warning("Insufficient funds detected");
            throw new PaymentProcessingException("Insufficient funds");
        }
        logger.info("Sufficient funds verified");
    }

    private void recordTransaction(PaymentDetails details, String transactionId) {
        logger.info(() -> String.format("Recording transaction: %s", transactionId));
        simulateNetworkCall(200);
        logger.info("Transaction recorded successfully");
    }

    private void simulateNetworkCall(int milliseconds) {
        try {
            logger.fine(() -> String.format("Simulating network call - Duration: %dms", milliseconds));
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Network call interrupted");
            throw new RuntimeException("Network call interrupted");
        }
    }
}
