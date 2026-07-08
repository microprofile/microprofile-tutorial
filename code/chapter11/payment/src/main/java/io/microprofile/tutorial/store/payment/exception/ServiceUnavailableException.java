package io.microprofile.tutorial.store.payment.exception;

/**
 * Exception thrown when the catalog service is temporarily unavailable.
 * 
 * This is an unchecked exception (RuntimeException) that can be thrown
 * without being declared in method signatures.
 * Used by ResponseExceptionMapper to map 503 HTTP responses.
 */
public class ServiceUnavailableException extends RuntimeException {
    
    private final int statusCode;

    public ServiceUnavailableException(String message) {
        super(message);
        this.statusCode = 503;
    }

    public ServiceUnavailableException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 503;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
