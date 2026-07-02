package io.microprofile.tutorial.store.payment.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Client Response Filter for comprehensive logging of incoming HTTP responses.
 * 
 * This filter demonstrates:
 * - Implementing ClientResponseFilter for response logging
 * - Using @Priority to control filter execution order
 * - Accessing both request and response contexts
 * - Logging response status, headers, and metadata
 * - Correlating responses with their originating requests
 * 
 * Priority 300 ensures this runs consistently with RequestLoggingFilter.
 * 
 * Response filters execute AFTER the HTTP response is received but BEFORE
 * entity deserialization, making them ideal for logging response metadata.
 */
@Priority(300)
public class ResponseLoggingFilter implements ClientResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ResponseLoggingFilter.class.getName());

    /**
     * Intercepts incoming HTTP responses for comprehensive logging.
     * 
     * Logs:
     * - Original request method and URI
     * - HTTP status code and reason phrase
     * - Response headers (including caching, content type)
     * - Correlation IDs for tracing
     * - Response timing information
     * 
     * This provides complete visibility into REST client interactions.
     * 
     * @param requestContext The original request context
     * @param responseContext The received response context
     */
    @Override
    public void filter(ClientRequestContext requestContext, 
                      ClientResponseContext responseContext) throws IOException {
        
        // Log basic response information
        LOGGER.info(String.format("=== Incoming REST Client Response ==="));
        LOGGER.info(String.format("Request: %s %s", 
                requestContext.getMethod(), 
                requestContext.getUri()));
        LOGGER.info(String.format("Status: %d %s", 
                responseContext.getStatus(),
                responseContext.getStatusInfo().getReasonPhrase()));
        
        // Log response content type
        if (responseContext.getMediaType() != null) {
            LOGGER.info(String.format("Content-Type: %s", 
                    responseContext.getMediaType()));
        }
        
        // Log content length if available
        int contentLength = responseContext.getLength();
        if (contentLength >= 0) {
            LOGGER.info(String.format("Content-Length: %d bytes", contentLength));
        }
        
        // Log important response headers
        LOGGER.info("Response Headers:");
        
        // Correlation/tracking headers
        String correlationId = responseContext.getHeaderString("X-Correlation-ID");
        if (correlationId != null) {
            LOGGER.info(String.format("  X-Correlation-ID: %s", correlationId));
        }
        
        String requestId = responseContext.getHeaderString("X-Request-ID");
        if (requestId != null) {
            LOGGER.info(String.format("  X-Request-ID: %s", requestId));
        }
        
        // Cache control headers
        String cacheControl = responseContext.getHeaderString("Cache-Control");
        if (cacheControl != null) {
            LOGGER.info(String.format("  Cache-Control: %s", cacheControl));
        }
        
        String etag = responseContext.getHeaderString("ETag");
        if (etag != null) {
            LOGGER.info(String.format("  ETag: %s", etag));
        }
        
        // Server information
        String server = responseContext.getHeaderString("Server");
        if (server != null) {
            LOGGER.info(String.format("  Server: %s", server));
        }
        
        // Log success or error status
        if (isSuccessful(responseContext.getStatus())) {
            LOGGER.info("Result: SUCCESS");
        } else if (isClientError(responseContext.getStatus())) {
            LOGGER.warning(String.format("Result: CLIENT ERROR (%d)", 
                    responseContext.getStatus()));
        } else if (isServerError(responseContext.getStatus())) {
            LOGGER.severe(String.format("Result: SERVER ERROR (%d)", 
                    responseContext.getStatus()));
        }
        
        LOGGER.info("====================================");
    }

    /**
     * Checks if the HTTP status code indicates success (2xx).
     */
    private boolean isSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Checks if the HTTP status code indicates a client error (4xx).
     */
    private boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    /**
     * Checks if the HTTP status code indicates a server error (5xx).
     */
    private boolean isServerError(int statusCode) {
        return statusCode >= 500 && statusCode < 600;
    }
}
