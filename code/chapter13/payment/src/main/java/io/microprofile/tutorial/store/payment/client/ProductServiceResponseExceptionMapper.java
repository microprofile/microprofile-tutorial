package io.microprofile.tutorial.store.payment.client;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;
import io.microprofile.tutorial.store.payment.exception.ServiceUnavailableException;

import java.io.InputStream;
import java.util.logging.Logger;

/**
 * ResponseExceptionMapper for ProductClient.
 * 
 * This class demonstrates:
 * - Mapping HTTP error responses to custom exceptions
 * - Extracting error messages from JSON response bodies
 * - Distinguishing between checked and unchecked exceptions
 * - Using @Priority for mapper ordering
 * 
 * HTTP Status Code Mappings:
 * - 404 → ProductNotFoundException (checked exception)
 * - 503 → ServiceUnavailableException (unchecked exception)
 * - 500-599 → RuntimeException for server errors
 * - 400-499 → RuntimeException for client errors
 */
@Priority(100)
public class ProductServiceResponseExceptionMapper implements ResponseExceptionMapper<Throwable> {
    
    private static final Logger LOGGER = Logger.getLogger(ProductServiceResponseExceptionMapper.class.getName());

    /**
     * Determines if this mapper shouldhandle the given response.
     * 
     * @param status HTTP status code
     * @param headers Response headers
     * @return true if status >= 400 (client and server errors)
     */
    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        // Handle all error responses (4xx and 5xx)
        boolean shouldHandle = status >= 400;
        if (shouldHandle) {
            LOGGER.info("ResponseExceptionMapper handling error response: " + status);
        }
        return shouldHandle;
    }

    /**
     * Converts HTTP response to an appropriate exception.
     * 
     * Demonstrates:
     * - Status code based exception mapping
     * - JSON error message extraction
     * - Checked vs unchecked exception handling
     * 
     * @param response The HTTP response
     * @return Throwable to be thrown by the REST client
     */
    @Override
    public Throwable toThrowable(Response response) {
        int status = response.getStatus();
        String errorMessage = extractErrorMessage(response);
        
        LOGGER.warning(String.format("Mapping HTTP %d to exception: %s", status, errorMessage));
        
        // Map specific status codes to custom exceptions
        switch (status) {
            case 404:
                // Checked exception - only thrown if client method declares it
                return new ProductNotFoundException("Product not found: " + errorMessage);
                
            case 503:
                // Unchecked exception - always thrown
                return new ServiceUnavailableException(
                    "Catalog service temporarily unavailable: " + errorMessage, status);
                
            case 500:
            case 502:
            case 504:
                // Server errors - unchecked exceptions
                return new RuntimeException(
                    String.format("Catalog service error (%d): %s", status, errorMessage));
                
            default:
                if (status >= 500) {
                    // Other 5xx errors
                    return new RuntimeException(
                        String.format("Server error (%d): %s", status, errorMessage));
                } else {
                    // Client errors (4xx)
                    return new RuntimeException(
                        String.format("Client error (%d): %s", status, errorMessage));
                }
        }
    }

    /**
     * Extracts error message from response body.
     * 
     * Attempts to parse JSON error response with structure:
     * { "error": "...", "message": "..." }
     * 
     * Falls back to generic message if parsing fails.
     * 
     * @param response The HTTP response
     * @return Extracted error message or default message
     */
    private String extractErrorMessage(Response response) {
        try {
            if (response.hasEntity()) {
                // Attempt to read JSON error response
                InputStream entityStream = response.readEntity(InputStream.class);
                JsonReader jsonReader = Json.createReader(entityStream);
                JsonObject errorJson = jsonReader.readObject();
                
                // Try multiple common error message field names
                if (errorJson.containsKey("message")) {
                    return errorJson.getString("message");
                } else if (errorJson.containsKey("error")) {
                    return errorJson.getString("error");
                } else if (errorJson.containsKey("errorMessage")) {
                    return errorJson.getString("errorMessage");
                }
            }
        } catch (Exception e) {
            // If JSON parsing fails, log and continue with default message
            LOGGER.fine("Failed to parse error response as JSON: " + e.getMessage());
        }
        
        // Default error message
        return "HTTP " + response.getStatus() + " - " + response.getStatusInfo().getReasonPhrase();
    }
}
