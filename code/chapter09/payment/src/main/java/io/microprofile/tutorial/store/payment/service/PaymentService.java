package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    private Tracer tracer;   // Injected tracer for OpenTelemetry

    @PostConstruct
    public void init() {
        // Programmatic tracer access - the correct approach
        this.tracer = GlobalOpenTelemetry.getTracer("payment-service", "1.0.0");
        logger.info("Tracer initialized successfully");
    }

    /**
     * Process the payment request with automatic tracing via MicroProfile Telemetry.
     * The mpTelemetry feature automatically creates spans for this method.
     *
     * @param paymentDetails details of the payment
     * @return response message indicating success or failure
     * @throws PaymentProcessingException if a transient issue occurs
     */
    @Asynchronous
    @Timeout(3000)
    @Retry(maxRetries = 3, delay = 2000, jitter = 500, retryOn = PaymentProcessingException.class, abortOn = CriticalPaymentException.class)
    @Fallback(fallbackMethod = "fallbackProcessPayment")
    @Bulkhead(value=5)
    public CompletionStage<String> processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException {
        // Create explicit span for payment processing to help with debugging
        Span span = tracer.spanBuilder("payment.process")
            .setAttribute("payment.amount", paymentDetails.getAmount().toString())
            .setAttribute("payment.method", "credit_card")
            .setAttribute("payment.service", "payment-service") 
            .startSpan();
        
        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            // MicroProfile Telemetry automatically traces this method
            String maskedCardNumber = maskCardNumber(paymentDetails.getCardNumber());
            
            logger.info(String.format("Processing payment - Amount: %s, Card: %s", 
                    paymentDetails.getAmount(), maskedCardNumber));
            
            span.addEvent("Starting payment processing");
            simulateDelay();

            // Simulating a transient failure
            if (Math.random() > 0.7) {
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Payment processing failed");
                span.addEvent("Payment processing failed due to transient error");
                logger.warning("Payment processing failed due to transient error");
                throw new PaymentProcessingException("Temporary payment processing failure");
            }

            // Simulating successful processing
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            span.addEvent("Payment processed successfully");
            logger.info("Payment processed successfully");
            return CompletableFuture.completedFuture("{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}");
        } finally {
            span.end();
        }
    }

    /**
     * Fallback method when payment processing fails.
     * Automatically traced by MicroProfile Telemetry.
     *
     * @param paymentDetails details of the payment
     * @return response message for fallback
     */
    public CompletionStage<String> fallbackProcessPayment(PaymentDetails paymentDetails) {
        logger.warning(() -> String.format("Fallback invoked for payment - Amount: %s", 
                paymentDetails.getAmount()));
        
        return CompletableFuture.completedFuture("{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
    }

    /**
     * Masks a credit card number for security in logs and traces.
     * Only the last 4 digits are shown, all others are replaced with 'X'.
     *
     * @param cardNumber The full card number
     * @return A masked card number (e.g., "XXXXXXXXXXXX1234")
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "INVALID_CARD";
        }
        int visibleDigits = 4;
        int length = cardNumber.length();
        
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < length - visibleDigits; i++) {
            masked.append('X');
        }
        masked.append(cardNumber.substring(length - visibleDigits));
        
        return masked.toString();
    }

    /**
     * Simulate a delay in processing to demonstrate timeout.
     * This method will be automatically traced by MicroProfile Telemetry.
     */
    private void simulateDelay() {
        try {
            logger.fine("Starting payment processing delay simulation");
            Thread.sleep(1500); // Simulated long-running task
            logger.fine("Payment processing delay simulation completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Payment processing interrupted");
            throw new RuntimeException("Processing interrupted");
        }
    }

    /**
     * Processes a comprehensive payment verification with multiple steps.
     * Each method call will be automatically traced by MicroProfile Telemetry.
     *
     * @param paymentDetails The payment details to verify
     * @param transactionId The unique transaction ID
     * @return A detailed verification result
     * @throws PaymentProcessingException if verification fails
     */
    @Asynchronous
    public CompletionStage<String> verifyPaymentWithTelemetry(PaymentDetails paymentDetails, String transactionId) 
            throws PaymentProcessingException {
        
        logger.info(() -> String.format("Starting payment verification - Transaction ID: %s", transactionId));
        
        try {
            // Step 1: Validate payment details
            validatePaymentDetails(paymentDetails);
            
            // Step 2: Check for fraud indicators
            performFraudCheck(paymentDetails, transactionId);
            
            // Step 3: Verify funds with bank
            verifyFundsAvailability(paymentDetails);
            
            // Step 4: Record transaction
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
    
    /**
     * Validates payment details - automatically traced
     */
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
    
    /**
     * Performs fraud check - automatically traced
     */
    private void performFraudCheck(PaymentDetails details, String transactionId) throws PaymentProcessingException {
        logger.info(() -> String.format("Performing fraud check for transaction: %s", transactionId));
        
        // Simulate external service call
        simulateNetworkCall(300);
        
        // Simulate fraud detection (cards ending with "0000" are flagged)
        boolean isSafe = !details.getCardNumber().endsWith("0000");
        
        if (!isSafe) {
            logger.warning("Potential fraud detected");
            throw new PaymentProcessingException("Fraud check failed");
        }
        
        logger.info("Fraud check passed");
    }
    
    /**
     * Verifies funds availability - automatically traced
     */
    private void verifyFundsAvailability(PaymentDetails details) throws PaymentProcessingException {
        logger.info(() -> String.format("Verifying funds availability - Amount: %s", details.getAmount()));
        
        // Simulate banking service call
        simulateNetworkCall(500);
        
        // Simulate funds verification (amounts over 1000 fail)
        boolean hasFunds = details.getAmount().doubleValue() <= 1000;
        
        if (!hasFunds) {
            logger.warning("Insufficient funds detected");
            throw new PaymentProcessingException("Insufficient funds");
        }
        
        logger.info("Sufficient funds verified");
    }
    
    /**
     * Records transaction - automatically traced
     */
    private void recordTransaction(PaymentDetails details, String transactionId) {
        logger.info(() -> String.format("Recording transaction: %s", transactionId));
        
        // Simulate database operation
        simulateNetworkCall(200);
        
        logger.info("Transaction recorded successfully");
    }
    
    /**
     * Simulates network calls or database operations - automatically traced
     */
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
