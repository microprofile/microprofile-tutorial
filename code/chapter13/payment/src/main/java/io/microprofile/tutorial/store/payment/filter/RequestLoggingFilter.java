package io.microprofile.tutorial.store.payment.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Client Request Filter for comprehensive logging of outgoing HTTP requests.
 * 
 * This filter demonstrates:
 * - Implementing ClientRequestFilter for request logging
 * - Using @Priority to control when the filter executes
 * - Accessing request metadata (method, URI, headers)
 * - Best practices for logging in filters
 * - Conditional header logging to avoid sensitive data exposure
 * 
 * Priority 300 ensures this runs after authentication and correlation ID filters,
 * so all headers are present and can be logged.
 * 
 * SECURITY NOTE: Be cautious when logging headers. Sensitive data like
 * Authorization tokens should never be fully logged in production.
 */
@Priority(300)
public class RequestLoggingFilter implements ClientRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(RequestLoggingFilter.class.getName());
    
    // Headers that should not be logged in full (for security)
    private static final String[] SENSITIVE_HEADERS = {
        "Authorization", "X-API-Key", "Cookie", "Set-Cookie"
    };

    /**
     * Intercepts outgoing HTTP requests for comprehensive logging.
     * 
     * Logs:
     * - HTTP method and target URI
     * - Request headers (with sensitive headers masked)
     * - Correlation/request tracking IDs
     * - Content type information
     * 
     * This provides visibility into REST client interactions for
     * debugging, monitoring, and troubleshooting.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Log basic request information
        LOGGER.info(String.format("=== Outgoing REST Client Request ==="));
        LOGGER.info(String.format("Method: %s", requestContext.getMethod()));
        LOGGER.info(String.format("URI: %s", requestContext.getUri()));
        
        // Log media type if present
        if (requestContext.getMediaType() != null) {
            LOGGER.info(String.format("Content-Type: %s", requestContext.getMediaType()));
        }
        
        // Log headers (with sensitive data masked)
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        LOGGER.info("Request Headers:");
        
        headers.forEach((headerName, headerValues) -> {
            if (isSensitiveHeader(headerName)) {
                // Mask sensitive headers
                LOGGER.info(String.format("  %s: [REDACTED]", headerName));
            } else {
                // Log non-sensitive headers
                headerValues.forEach(value -> 
                    LOGGER.info(String.format("  %s: %s", headerName, value))
                );
            }
        });
        
        // Log entity class if present (for POST/PUT requests)
        if (requestContext.hasEntity()) {
            LOGGER.info(String.format("Entity Type: %s", 
                    requestContext.getEntity().getClass().getSimpleName()));
        }
        
        LOGGER.info("===================================");
    }

    /**
     * Checks if a header contains sensitive information that should not be logged.
     * 
     * @param headerName The header name to check
     * @return true if the header is sensitive, false otherwise
     */
    private boolean isSensitiveHeader(String headerName) {
        for (String sensitiveHeader : SENSITIVE_HEADERS) {
            if (sensitiveHeader.equalsIgnoreCase(headerName)) {
                return true;
            }
        }
        return false;
    }
}
