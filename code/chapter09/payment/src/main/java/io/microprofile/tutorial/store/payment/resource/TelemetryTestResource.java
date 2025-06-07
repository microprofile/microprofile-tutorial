package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.service.TelemetryTestService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Scope;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * REST endpoint to test manual telemetry instrumentation with enhanced observability
 * Demonstrates both automatic JAX-RS instrumentation and manual span creation
 */
@RequestScoped
@Path("/test-telemetry")
@Produces(MediaType.APPLICATION_JSON)
public class TelemetryTestResource {

    private static final Logger logger = Logger.getLogger(TelemetryTestResource.class.getName());
    private Tracer resourceTracer;

    @Inject
    TelemetryTestService telemetryTestService;

    @PostConstruct
    public void init() {
        // Initialize tracer for resource-level spans
        this.resourceTracer = GlobalOpenTelemetry.getTracer("payment-test-resource", "1.0.0");
        logger.info("TelemetryTestResource tracer initialized");
    }

    @GET
    @Path("/basic")
    public Response testBasic(@QueryParam("orderId") @DefaultValue("TEST001") String orderId,
                              @QueryParam("amount") @DefaultValue("99.99") double amount) {
        
        // Create resource-level span for enhanced observability
        Span resourceSpan = resourceTracer.spanBuilder("telemetry.test.basic.endpoint")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        
        try (Scope scope = resourceSpan.makeCurrent()) {
            // Add resource-level attributes
            resourceSpan.setAttribute("test.type", "basic");
            resourceSpan.setAttribute("test.endpoint", "/test-telemetry/basic");
            resourceSpan.setAttribute("order.id", orderId);
            resourceSpan.setAttribute("payment.amount", amount);
            resourceSpan.setAttribute("http.method", "GET");
            
            resourceSpan.addEvent("Basic telemetry test started");
            
            String result = telemetryTestService.testBasicSpan(orderId, amount);
            
            resourceSpan.addEvent("Service call completed");
            resourceSpan.setAttribute("test.result", "success");
            
            return Response.ok()
                    .entity("{\"status\":\"success\", \"message\":\"" + result + "\"}")
                    .build();
                    
        } catch (Exception e) {
            resourceSpan.recordException(e);
            resourceSpan.setAttribute("test.result", "error");
            resourceSpan.setAttribute("error.message", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}")
                    .build();
        } finally {
            resourceSpan.end();
        }
    }

    @GET
    @Path("/advanced")
    public Response testAdvanced(@QueryParam("customerId") @DefaultValue("CUST001") String customerId) {
        
        // Create resource-level span for enhanced observability
        Span resourceSpan = resourceTracer.spanBuilder("telemetry.test.advanced.endpoint")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        
        try (Scope scope = resourceSpan.makeCurrent()) {
            // Add resource-level attributes
            resourceSpan.setAttribute("test.type", "advanced");
            resourceSpan.setAttribute("test.endpoint", "/test-telemetry/advanced");
            resourceSpan.setAttribute("customer.id", customerId);
            resourceSpan.setAttribute("http.method", "GET");
            
            resourceSpan.addEvent("Advanced telemetry test started");
            
            String result = telemetryTestService.testAdvancedSpan(customerId);
            
            resourceSpan.addEvent("Service call completed");
            resourceSpan.setAttribute("test.result", "success");
            
            return Response.ok()
                    .entity("{\"status\":\"success\", \"message\":\"" + result + "\"}")
                    .build();
                    
        } catch (Exception e) {
            resourceSpan.recordException(e);
            resourceSpan.setAttribute("test.result", "error");
            resourceSpan.setAttribute("error.message", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}")
                    .build();
        } finally {
            resourceSpan.end();
        }
    }

    @GET
    @Path("/nested")
    public Response testNested(@QueryParam("requestId") @DefaultValue("REQ001") String requestId) {
        
        // Create resource-level span for enhanced observability
        Span resourceSpan = resourceTracer.spanBuilder("telemetry.test.nested.endpoint")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        
        try (Scope scope = resourceSpan.makeCurrent()) {
            // Add resource-level attributes
            resourceSpan.setAttribute("test.type", "nested");
            resourceSpan.setAttribute("test.endpoint", "/test-telemetry/nested");
            resourceSpan.setAttribute("request.id", requestId);
            resourceSpan.setAttribute("http.method", "GET");
            
            resourceSpan.addEvent("Nested telemetry test started");
            
            String result = telemetryTestService.testNestedSpans(requestId);
            
            resourceSpan.addEvent("Service call completed");
            resourceSpan.setAttribute("test.result", "success");
            
            return Response.ok()
                    .entity("{\"status\":\"success\", \"message\":\"" + result + "\"}")
                    .build();
                    
        } catch (Exception e) {
            resourceSpan.recordException(e);
            resourceSpan.setAttribute("test.result", "error");
            resourceSpan.setAttribute("error.message", e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}")
                    .build();
        } finally {
            resourceSpan.end();
        }
    }

    /**
     * Comprehensive telemetry test endpoint that demonstrates all features together
     */
    @GET
    @Path("/comprehensive")
    public Response testComprehensive(@QueryParam("transactionId") @DefaultValue("TXN001") String transactionId,
                                    @QueryParam("customerId") @DefaultValue("CUST001") String customerId,
                                    @QueryParam("amount") @DefaultValue("199.99") double amount) {
        
        // Create resource-level span with comprehensive attributes
        Span resourceSpan = resourceTracer.spanBuilder("telemetry.test.comprehensive.endpoint")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        
        try (Scope scope = resourceSpan.makeCurrent()) {
            // Add comprehensive resource-level attributes
            resourceSpan.setAttribute("test.type", "comprehensive");
            resourceSpan.setAttribute("test.endpoint", "/test-telemetry/comprehensive");
            resourceSpan.setAttribute("transaction.id", transactionId);
            resourceSpan.setAttribute("customer.id", customerId);
            resourceSpan.setAttribute("payment.amount", amount);
            resourceSpan.setAttribute("http.method", "GET");
            resourceSpan.setAttribute("service.version", "1.0.0");
            resourceSpan.setAttribute("test.complexity", "high");
            
            // Add events to track the test progress
            resourceSpan.addEvent("Comprehensive telemetry test started");
            
            // Test all telemetry features in sequence
            resourceSpan.addEvent("Testing basic span creation");
            String basicResult = telemetryTestService.testBasicSpan(transactionId, amount);
            
            resourceSpan.addEvent("Testing advanced span creation");
            String advancedResult = telemetryTestService.testAdvancedSpan(customerId);
            
            resourceSpan.addEvent("Testing nested span creation");
            String nestedResult = telemetryTestService.testNestedSpans(transactionId);
            
            resourceSpan.addEvent("All telemetry tests completed successfully");
            resourceSpan.setAttribute("test.result", "success");
            resourceSpan.setAttribute("tests.executed", 3);
            
            // Create comprehensive response
            String comprehensiveResult = String.format(
                "Comprehensive telemetry test completed. Basic: %s, Advanced: %s, Nested: %s",
                basicResult, advancedResult, nestedResult
            );
            
            return Response.ok()
                    .entity("{\"status\":\"success\", \"message\":\"" + comprehensiveResult + "\", \"transactionId\":\"" + transactionId + "\"}")
                    .build();
                    
        } catch (Exception e) {
            resourceSpan.recordException(e);
            resourceSpan.setAttribute("test.result", "error");
            resourceSpan.setAttribute("error.message", e.getMessage());
            resourceSpan.setAttribute("error.type", e.getClass().getSimpleName());
            resourceSpan.addEvent("Test failed with exception: " + e.getMessage());
            
            logger.severe("Comprehensive telemetry test failed: " + e.getMessage());
            
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\", \"transactionId\":\"" + transactionId + "\"}")
                    .build();
        } finally {
            resourceSpan.end();
        }
    }
}
