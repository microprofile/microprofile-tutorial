package io.microprofile.tutorial.store.shoppingcart.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ShoppingCart class for the microprofile tutorial store application.
 * This class represents a user's shopping cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCart {

    private Long cartId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;
    
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    /**
     * Calculate the total number of items in the cart.
     * 
     * @return The total number of items
     */
    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    /**
     * Calculate the total price of all items in the cart.
     * 
     * @return The total price
     */
    public double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}
