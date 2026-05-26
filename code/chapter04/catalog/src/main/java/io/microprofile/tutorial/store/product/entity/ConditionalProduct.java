package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.DependentRequired;

/**
 * Product with conditional validation.
 * Demonstrates the @DependentRequired annotation in MicroProfile OpenAPI 4.1.
 */
@Schema(
    description = "Product with conditional validation",
    dependentRequired = {
        @DependentRequired(
            name = "discount",
            requires = {"discountReason"}
        )
    }
)
public class ConditionalProduct {
    
    @Schema(description = "Product ID", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "Laptop", required = true)
    private String name;
    
    @Schema(description = "Product price", example = "999.99", required = true)
    private Double price;
    
    @Schema(description = "Discount amount (requires discountReason if set)", example = "100.0")
    private Double discount;
    
    @Schema(description = "Reason for discount", example = "Black Friday Sale")
    private String discountReason;
    
    public ConditionalProduct() {
    }
    
    public ConditionalProduct(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Double getDiscount() {
        return discount;
    }
    
    public void setDiscount(Double discount) {
        this.discount = discount;
    }
    
    public String getDiscountReason() {
        return discountReason;
    }
    
    public void setDiscountReason(String discountReason) {
        this.discountReason = discountReason;
    }
}
