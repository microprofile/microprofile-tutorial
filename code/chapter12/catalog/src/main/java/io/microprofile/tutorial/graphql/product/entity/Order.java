package io.microprofile.tutorial.graphql.product.entity;

import org.eclipse.microprofile.graphql.Type;
import org.eclipse.microprofile.graphql.Description;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Order entity representing a product order
 */
@Type("Order")
@Description("An order for products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Description("Unique order identifier")
    private Long id;
    
    @Description("Product ID being ordered")
    private Long productId;
    
    @Description("Quantity ordered")
    private Integer quantity;
    
    @Description("Order status")
    private String status;
    
    @Description("Order creation date")
    private LocalDateTime createdAt;
}
