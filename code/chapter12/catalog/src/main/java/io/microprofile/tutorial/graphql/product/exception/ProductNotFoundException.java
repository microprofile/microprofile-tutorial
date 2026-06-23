package io.microprofile.tutorial.graphql.product.exception;

/**
 * Custom exception for when a product is not found
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found: " + productId);
    }
}