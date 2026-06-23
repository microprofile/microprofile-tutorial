package io.microprofile.tutorial.graphql.product.service;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Service for pricing calculations including discounts
 */
@ApplicationScoped
public class PricingService {
    
    /**
     * Calculate discounted price based on discount code
     * 
     * @param originalPrice The original price
     * @param discountCode The discount code to apply
     * @return Discounted price
     */
    public Double calculateDiscountedPrice(Double originalPrice, String discountCode) {
        if (originalPrice == null || discountCode == null) {
            return originalPrice;
        }
        
        // Simple discount logic based on code
        double discountRate = switch (discountCode.toUpperCase()) {
            case "SAVE10" -> 0.10;  // 10% off
            case "SAVE20" -> 0.20;  // 20% off
            case "SAVE30" -> 0.30;  // 30% off
            case "HALF" -> 0.50;    // 50% off
            default -> 0.0;         // No discount
        };
        
        return originalPrice * (1 - discountRate);
    }
    
    /**
     * Calculate special promotional price for a product
     * Throws exception for certain products to demonstrate partial results
     * 
     * @param productId The product ID
     * @return Special price
     * @throws RuntimeException for product IDs divisible by 3 (for demonstration)
     */
    public Double calculateSpecialPrice(Long productId) {
        // Simulate failure for certain products to demonstrate partial results
        if (productId % 3 == 0) {
            throw new RuntimeException("Special pricing service temporarily unavailable for product " + productId);
        }
        
        // Return a 10% discount for other products
        return null; // This will be calculated per product
    }
}
