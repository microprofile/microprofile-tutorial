package io.microprofile.tutorial.graphql.product.exception;

import org.eclipse.microprofile.graphql.GraphQLException;

/**
 * Custom GraphQL exception for insufficient stock scenarios
 * Demonstrates using GraphQLException with descriptive error messages
 */
public class InsufficientStockException extends GraphQLException {
    
    private final Long productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(Long productId, int requested, int available) {
        super(String.format("Insufficient stock for product %d: requested %d, available %d",
                          productId, requested, available),
              GraphQLException.ExceptionType.DataFetchingException);
        this.productId = productId;
        this.requestedQuantity = requested;
        this.availableQuantity = available;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
