package io.microprofile.tutorial.store.product.entity;

import java.util.Optional;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Product with optional fields.
 * Demonstrates MicroProfile OpenAPI 4.1 support for Optional<T> types.
 */
@Schema(description = "Product with optional fields")
public class ProductWithOptional {
    
    @Schema(description = "Product ID", required = true)
    private Long id;
    
    @Schema(description = "Product name", required = true)
    private String name;
    
    @Schema(description = "Optional product description")
    private Optional<String> description = Optional.empty();
    
    @Schema(description = "Optional product category")
    private Optional<String> category = Optional.empty();
    
    @Schema(description = "Product price", required = true)
    private Double price;
    
    public ProductWithOptional() {
    }
    
    public ProductWithOptional(Long id, String name, Double price) {
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
    
    public Optional<String> getDescription() { 
        return description; 
    }
    
    public void setDescription(Optional<String> description) { 
        this.description = description; 
    }
    
    public Optional<String> getCategory() { 
        return category; 
    }
    
    public void setCategory(Optional<String> category) { 
        this.category = category; 
    }
    
    public Double getPrice() { 
        return price; 
    }
    
    public void setPrice(Double price) { 
        this.price = price; 
    }
    
    /**
     * Create from a regular Product.
     */
    public static ProductWithOptional fromProduct(Product product) {
        ProductWithOptional p = new ProductWithOptional(
            product.getId(), 
            product.getName(), 
            product.getPrice()
        );
        p.setDescription(Optional.ofNullable(product.getDescription()));
        return p;
    }
}
