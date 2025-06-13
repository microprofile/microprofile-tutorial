package io.microprofile.tutorial.store.payment.examples;

import io.microprofile.tutorial.store.payment.client.ProductClientJson;
import io.microprofile.tutorial.store.payment.dto.product.Product;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Example demonstrating how to use the ProductClientJson.getProductsWithJsonp method.
 */
public class ProductClientExample {
    
    private static final Logger LOGGER = Logger.getLogger(ProductClientExample.class.getName());

    public static void main(String[] args) {
        
        // Example 1: Call with default URL (http://localhost:5050/products)
        LOGGER.info("=== Example 1: Using default URL ===");
        try {
            Product[] products = ProductClientJson.getProductsWithJsonp(null);
            printProducts("Default URL", products);
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch products with default URL: " + e.getMessage());
        }

        // Example 2: Call with custom catalog service URL
        LOGGER.info("=== Example 2: Using custom catalog service URL ===");
        try {
            String catalogUrl = "http://localhost:5050/catalog/api/products";
            Product[] products = ProductClientJson.getProductsWithJsonp(catalogUrl);
            printProducts("Custom catalog URL", products);
        } catch (Exception e) {
            LOGGER.warning("Failed to fetch products from catalog service: " + e.getMessage());
        }

        // Example 3: Call with different environment URLs
        LOGGER.info("=== Example 3: Using different environment URLs ===");
        String[] environmentUrls = {
            "http://localhost:5050/catalog/api/products",  // Local catalog service
            "http://localhost:6050/products",              // Alternative port
            "https://api.example.com/products"             // External API
        };

        for (String url : environmentUrls) {
            try {
                LOGGER.info("Trying URL: " + url);
                Product[] products = ProductClientJson.getProductsWithJsonp(url);
                printProducts("URL: " + url, products);
                break; // Stop on first successful call
            } catch (Exception e) {
                LOGGER.warning("Failed to fetch from " + url + ": " + e.getMessage());
            }
        }
    }

    /**
     * Helper method to print product information
     */
    private static void printProducts(String source, Product[] products) {
        LOGGER.info("Products from " + source + ":");
        if (products != null && products.length > 0) {
            Arrays.stream(products)
                .forEach(product -> LOGGER.info("  " + product.toString()));
            LOGGER.info("Total products found: " + products.length);
        } else {
            LOGGER.info("  No products found");
        }
        System.out.println(); // Add blank line for readability
    }
}
