package io.microprofile.tutorial.store.payment.service;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

/**
 * Simplified test service to verify manual instrumentation concepts from the tutorial
 */
@ApplicationScoped
public class TelemetryTestService {

    private static final Logger logger = Logger.getLogger(TelemetryTestService.class.getName());

    private Tracer tracer;  // Get tracer programmatically instead of injection

    @PostConstruct
    public void init() {
        // Step 2: Get tracer programmatically from GlobalOpenTelemetry
        logger.info("Initializing Tracer from GlobalOpenTelemetry...");
        this.tracer = GlobalOpenTelemetry.getTracer("payment-service-manual", "1.0.0");
        logger.info("Tracer initialized successfully: " + tracer.getClass().getName());
        logger.info("GlobalOpenTelemetry class: " + GlobalOpenTelemetry.get().getClass().getName());
        
        // Test creating a simple span to verify tracer works
        try {
            Span testSpan = tracer.spanBuilder("initialization-test").startSpan();
            testSpan.setAttribute("test", "initialization");
            testSpan.end();
            logger.info("Test span created successfully during initialization");
        } catch (Exception e) {
            logger.severe("Failed to create test span: " + e.getMessage());
        }
    }

    /**
     * Step 3: Test basic span creation and management
     */
    public String testBasicSpan(String orderId, double amount) {
        logger.info("Starting basic span test for order: " + orderId);
        
        // Create a span for a specific operation
        Span span = tracer.spanBuilder("payment.test.basic")
            .setSpanKind(SpanKind.INTERNAL)
            .startSpan();

        try {
            // Step 4: Add attributes to the span
            span.setAttribute("order.id", orderId);
            span.setAttribute("payment.amount", amount);
            span.setAttribute("test.type", "basic_span");

            // Simulate some work
            Thread.sleep(50);
            
            span.setAttribute("operation.status", "SUCCESS");
            logger.info("Basic span test completed successfully");
            return "Basic span test completed for order: " + orderId;

        } catch (Exception e) {
            // Step 5: Proper exception handling
            span.setAttribute("operation.status", "FAILED");
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.recordException(e);
            logger.severe("Basic span test failed: " + e.getMessage());
            throw new RuntimeException("Test failed", e);
        } finally {
            span.end();  // Always end the span
        }
    }

    /**
     * Test span with events and advanced attributes
     */
    public String testAdvancedSpan(String customerId) {
        logger.info("Starting advanced span test for customer: " + customerId);
        
        Span span = tracer.spanBuilder("payment.test.advanced")
            .setSpanKind(SpanKind.INTERNAL)
            .setAttribute("customer.id", customerId)
            .startSpan();

        try {
            // Add events to mark important moments
            span.addEvent("Starting customer validation");
            
            // Simulate validation
            Thread.sleep(30);
            
            span.addEvent("Customer validation completed");
            span.setAttribute("validation.result", "success");
            
            logger.info("Advanced span test completed successfully");
            return "Advanced span test completed for customer: " + customerId;

        } catch (Exception e) {
            span.setAttribute("error", true);
            span.setAttribute("error.type", e.getClass().getSimpleName());
            span.recordException(e);
            logger.severe("Advanced span test failed: " + e.getMessage());
            throw new RuntimeException("Advanced test failed", e);
        } finally {
            span.end();
        }
    }

    /**
     * Test nested spans to show parent-child relationships
     */
    public String testNestedSpans(String requestId) {
        logger.info("Starting nested spans test for request: " + requestId);
        
        Span parentSpan = tracer.spanBuilder("payment.test.parent")
            .startSpan();

        try {
            parentSpan.setAttribute("request.id", requestId);
            parentSpan.addEvent("Starting parent operation");
            
            // Make the parent span current, so child spans will be properly linked
            try (var scope = parentSpan.makeCurrent()) {
                // Create child span
                Span childSpan = tracer.spanBuilder("payment.test.child")
                    .startSpan();
                
                try {
                    childSpan.setAttribute("child.operation", "validation");
                    childSpan.addEvent("Performing child operation");
                    
                    // Simulate child work
                    Thread.sleep(25);
                    
                    childSpan.setAttribute("child.status", "completed");
                    
                } finally {
                    childSpan.end();
                }
            }
            
            parentSpan.addEvent("Parent operation completed");
            parentSpan.setAttribute("parent.status", "success");
            
            logger.info("Nested spans test completed successfully");
            return "Nested spans test completed for request: " + requestId;

        } catch (Exception e) {
            parentSpan.setAttribute("error", true);
            parentSpan.recordException(e);
            logger.severe("Nested spans test failed: " + e.getMessage());
            throw new RuntimeException("Nested spans test failed", e);
        } finally {
            parentSpan.end();
        }
    }
}
