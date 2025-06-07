package io.microprofile.tutorial.store.shoppingcart.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CartItem class for the microprofile tutorial store application.
 * This class represents an item in a shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    private Long itemId;

    @NotNull(message = "Product ID cannot be null")
    private Long productId;
    
    private String productName;
    
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private double price;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
