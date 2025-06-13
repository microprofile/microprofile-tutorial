package io.microprofile.tutorial.store.inventory.dto;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Product DTO for the inventory service.
 * This class represents product information received from the product service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    /**
     * Unique identifier for the product.
     */
    private Long id;
    
    /**
     * Name of the product.
     */
    private String name;
    
    /**
     * Price of the product.
     */
    private Double price;
    
    /**
     * Category of the product.
     */
    private String category;
    
    /**
     * Description of the product.
     */
    private String description;
    
    /**
     * Availability status of the product.
     */
    @JsonbTransient
    private boolean isAvailable = true;

    @JsonbCreator
    public Product(
            @JsonbProperty("id") Long id, 
            @JsonbProperty("name") String name, 
            @JsonbProperty("price") Double price,
            @JsonbProperty("category") String category,
            @JsonbProperty("description") String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%.2f, category='%s', isAvailable=%b}", 
                id, name, price, category, isAvailable);
    }
}
