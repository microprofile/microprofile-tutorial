package io.microprofile.tutorial.store.payment.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Client Request Filter that adds or propagates correlation IDs for distributed tracing.
 * 
 * This filter demonstrates:
 * - Implementing ClientRequestFilter interface
 * - Using @Priority to control filter execution order
 * - Adding custom headers to outgoing requests
 * - Generating UUIDs for request tracking
 * - Header propagation patterns in microservices
 * 
 * Correlation IDs enable:
 * - Tracing requests across multiple microservices
 * - Debugging distributed transactions
 * - Correlating logs from different services
 * - Performance monitoring and analytics
 * 
 * Priority 100 ensures this runs early in the filter chain.
 */
@Priority(100)
public class CorrelationIdFilter implements ClientRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(CorrelationIdFilter.class.getName());
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    /**
     * Intercepts outgoing HTTP requests to add correlation tracking headers.
     * 
     * The filter:
     * 1. Checks if X-Correlation-ID already exists in the request
     * 2. If not present, generates a new UUID for the correlation ID
     * 3. Always generates a unique request ID for this specific request
     * 4. Adds both headers to the outgoing HTTP request
     * 
     * This enables end-to-end request tracing across microservices.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        
        // Check if correlation ID already exists (propagated from incoming request)
        String correlationId = (String) headers.getFirst(CORRELATION_ID_HEADER);
        
        if (correlationId == null || correlationId.isEmpty()) {
            // Generate new correlation ID for this request chain
            correlationId = UUID.randomUUID().toString();
            headers.putSingle(CORRELATION_ID_HEADER, correlationId);
            LOGGER.info("Generated new Correlation ID: " + correlationId);
        } else {
            LOGGER.info("Propagating existing Correlation ID: " + correlationId);
        }
        
        // Always generate a unique request ID for this specific HTTP request
        String requestId = UUID.randomUUID().toString();
        headers.putSingle(REQUEST_ID_HEADER, requestId);
        
        LOGGER.fine(String.format("Request headers added - Correlation-ID: %s, Request-ID: %s", 
                correlationId, requestId));
        
        // Log request details with correlation context
        LOGGER.info(String.format("[%s] Outgoing request: %s %s",
                correlationId,
                requestContext.getMethod(),
                requestContext.getUri()));
    }
}
