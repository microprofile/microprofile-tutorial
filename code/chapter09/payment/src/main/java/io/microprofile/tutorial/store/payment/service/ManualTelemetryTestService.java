package io.microprofile.tutorial.store.payment.service;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Logger;

/**
 * Test service to verify manual instrumentation concepts from the tutorial
 */
@ApplicationScoped
public class ManualTelemetryTestService {

    private static final Logger logger = Logger.getLogger(ManualTelemetryTestService.class.getName());

    @Inject
    Tracer tracer;  // Step 2: Tracer injection verification

    /**
     * Step 3: Test basic span creation and management
     */
    public String testBasicSpanCreation(String orderId, double amount) {
        // Create a span for a specific operation
        Span span = tracer.spanBuilder("test.basic.operation")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try {
            // Step 4: Add attributes to the span
            span.setAttribute("order.id", orderId);
            span.setAttribute("payment.amount", amount);
            span.setAttribute("test.type", "basic_span_creation");

            // Business logic simulation
            performTestOperation(orderId, amount);

            span.setAttribute("operation.status", "SUCCESS");
            return "Basic span test completed successfully";

        } catch (Exception e) {
            // Step 5: Proper exception handling
            span.setAttribute("operation.status", "FAILED");
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.recordException(e);
            throw e;
        } finally {
            span.end();  // Always end the span
        }
    }

    /**
     * Step 3: Test advanced span configuration
     */
    public String testAdvancedSpanConfiguration(String customerId, String productId) {
        Span span = tracer.spanBuilder("test.advanced.operation")
            .setSpanKind(SpanKind.CLIENT)  // This operation calls external service
            .setAttribute("customer.id", customerId)
            .setAttribute("product.id", productId)
            .setAttribute("operation.type", "advanced_test")
            .startSpan();

        try {
            // Add dynamic attributes based on business logic
            if (isPremiumCustomer(customerId)) {
                span.setAttribute("customer.tier", "premium");
                span.setAttribute("priority.level", "high");
            }

            // Add events to mark important moments
            span.addEvent("Starting advanced test operation");
            
            boolean result = performAdvancedOperation(productId);
            
            span.addEvent("Advanced test operation completed");
            span.setAttribute("operation.available", result);
            span.setAttribute("operation.result", result ? "success" : "failure");

            return "Advanced span test completed";

        } catch (Exception e) {
            // Record exceptions with context
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Step 5: Test span lifecycle management with proper exception handling
     */
    public String testSpanLifecycleManagement(String requestId) {
        Span span = tracer.spanBuilder("test.lifecycle.management").startSpan();
        
        try (var scope = span.makeCurrent()) {  // Make span current for context propagation
            span.setAttribute("request.id", requestId);
            span.setAttribute("processing.type", "lifecycle_test");
            
            // Add event to mark processing start
            span.addEvent("Lifecycle test started");
            
            // Business logic here
            String result = performLifecycleTest(requestId);
            
            // Mark successful completion
            span.setAttribute("processing.status", "completed");
            span.addEvent("Lifecycle test completed successfully");
            
            return result;
            
        } catch (IllegalArgumentException e) {
            span.setAttribute("error.category", "validation");
            span.addEvent("Validation error occurred");
            addErrorContext(span, e);
            throw e;
            
        } catch (RuntimeException e) {
            span.setAttribute("error.category", "runtime");
            span.addEvent("Runtime error occurred");
            addErrorContext(span, e);
            throw e;
            
        } catch (Exception e) {
            span.setAttribute("error.category", "unexpected");
            span.addEvent("Unexpected error during processing");
            addErrorContext(span, e);
            throw e;
            
        } finally {
            span.end();
        }
    }

    // Helper methods for testing
    private void performTestOperation(String orderId, double amount) {
        logger.info(String.format("Performing test operation for order %s with amount %s", orderId, amount));
        // Simulate some work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isPremiumCustomer(String customerId) {
        // Simple test logic
        return customerId != null && customerId.startsWith("PREM");
    }

    private boolean performAdvancedOperation(String productId) {
        logger.info(String.format("Performing advanced operation for product %s", productId));
        // Simulate some work and return success
        return Math.random() > 0.2; // 80% success rate
    }

    private String performLifecycleTest(String requestId) {
        if (requestId == null || requestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Request ID cannot be null or empty");
        }
        
        if (requestId.equals("RUNTIME_ERROR")) {
            throw new RuntimeException("Simulated runtime error");
        }
        
        return "Lifecycle test result for: " + requestId;
    }

    private void addErrorContext(Span span, Exception error) {
        span.setAttribute("error", true);
        span.setAttribute("error.type", error.getClass().getSimpleName());
        span.setAttribute("error.message", error.getMessage());
        span.recordException(error);
    }
}
