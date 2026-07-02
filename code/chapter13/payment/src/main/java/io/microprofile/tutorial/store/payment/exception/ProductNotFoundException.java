package io.microprofile.tutorial.store.payment.exception;

/**
 * Exception thrown when a product is not found in the catalog service.
 * 
 * This is a checked exception that must be declared in method signatures.
 * Used by ResponseExceptionMapper to map 404 HTTP responses.
 */
public class ProductNotFoundException extends Exception {
    
    private final Long productId;

    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }

    public ProductNotFoundException(String message, Long productId) {
        super(message);
        this.productId = productId;
    }

    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.productId = null;
    }

    public Long getProductId() {
        return productId;
    }
}
