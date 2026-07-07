package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Product information as a Java Record.
 * Demonstrates MicroProfile OpenAPI 4.1 support for Java Records.
 */
@Schema(name = "ProductRecord", description = "Product information as a Java Record")
public record ProductRecord(
    @Schema(description = "Product ID", example = "1")
    Long id,
    
    @Schema(description = "Product name", example = "Laptop", required = true)
    String name,
    
    @Schema(description = "Product description", example = "High-performance laptop")
    String description,
    
    @Schema(description = "Product price", example = "999.99", required = true)
    Double price
) {
    /**
     * Create a ProductRecord from a Product entity.
     * 
     * @param product the product entity
     * @return a new ProductRecord
     */
    public static ProductRecord fromProduct(Product product) {
        return new ProductRecord(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice()
        );
    }
}
