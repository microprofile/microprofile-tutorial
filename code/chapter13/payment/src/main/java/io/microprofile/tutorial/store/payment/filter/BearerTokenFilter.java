package io.microprofile.tutorial.store.payment.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.Priorities;

import org.eclipse.microprofile.config.ConfigProvider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Client Request Filter for Bearer token authentication.
 * 
 * This filter demonstrates:
 * - Implementing authentication via ClientRequestFilter
 * - Using @Priority for proper filter ordering
 * - Reading configuration from MicroProfile Config
 * - Adding Authorization headers to outgoing requests
 * - Best practices for handling authentication tokens
 * 
 * Priority set to Priorities.AUTHENTICATION (1000) ensures this runs
 * before most other filters, as authentication should happen first.
 * 
 * The Bearer token is retrieved from MicroProfile Config, allowing
 * different tokens for different environments without code changes.
 * 
 * Configuration property:
 * - catalog-service.bearer.token (optional)
 */
@Priority(Priorities.AUTHENTICATION)
public class BearerTokenFilter implements ClientRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(BearerTokenFilter.class.getName());
    private static final String TOKEN_CONFIG_KEY = "catalog-service.bearer.token";

    /**
     * Intercepts outgoing HTTP requests to add Bearer token authentication.
     * 
     * The filter:
     * 1. Retrieves the Bearer token from MicroProfile Config
     * 2. If token is configured, adds Authorization header with Bearer scheme
     * 3. Logs authentication status (without exposing the token)
     * 
     * If no token is configured, the request proceeds without authentication.
     * This allows the same code to work in both authenticated and
     * non-authenticated environments.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // Retrieve Bearer token from configuration
        String bearerToken = ConfigProvider.getConfig()
                .getOptionalValue(TOKEN_CONFIG_KEY, String.class)
                .orElse(null);
        
        if (bearerToken != null && !bearerToken.isEmpty()) {
            // Add Authorization header with Bearer token
            requestContext.getHeaders()
                    .putSingle("Authorization", "Bearer " + bearerToken);
            
            LOGGER.info("Bearer token authentication added to request");
            
            // Log token length (not the actual token for security)
            LOGGER.fine(String.format("Token length: %d characters", bearerToken.length()));
            
        } else {
            // No token configured - proceed without authentication
            LOGGER.fine("No Bearer token configured - request sent without authentication");
        }
    }
}
