package io.microprofile.tutorial.graphql.product.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing product inventory levels
 */
@ApplicationScoped
public class InventoryService {
    
    private final Map<Long, Integer> stockLevels = new ConcurrentHashMap<>();
    
    /**
     * Get current stock level for a product
     * 
     * @param productId The product ID
     * @return Current stock level
     */
    public int getStockLevel(Long productId) {
        // Return random stock levels for demonstration
        // In a real application, this would query a database
        return stockLevels.computeIfAbsent(productId, id -> {
            // Simulate different stock levels
            if (id % 3 == 0) return 5;  // Low stock
            if (id % 3 == 1) return 0;  // Out of stock
            return 50; // Normal stock
        });
    }
    
    /**
     * Update stock level after an order
     * 
     * @param productId The product ID
     * @param quantity Quantity to deduct
     */
    public void deductStock(Long productId, int quantity) {
        int currentStock = getStockLevel(productId);
        stockLevels.put(productId, currentStock - quantity);
    }
}
