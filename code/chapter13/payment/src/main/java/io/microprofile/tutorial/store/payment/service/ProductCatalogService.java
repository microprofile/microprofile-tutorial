package io.microprofile.tutorial.store.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.microprofile.tutorial.store.payment.client.ProductClient;
import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;
import io.microprofile.tutorial.store.payment.exception.ServiceUnavailableException;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service demonstrating CDI injection of MicroProfile Rest Client.
 * 
 * This class showcases:
 * - @Inject annotation for dependency injection
 * - @RestClient qualifier (mandatory in MicroProfile Rest Client 4.0)
 * - @ApplicationScoped for efficient singleton pattern
 * - Type-safe REST client usage without manual instantiation
 * - Automatic configuration via MicroProfile Config
 * 
 * The ProductClient is automatically configured using properties from 
 * microprofile-config.properties (catalog-service/mp-rest/*)
 */
@ApplicationScoped
public class ProductCatalogService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductCatalogService.class.getName());

    /**
     * MicroProfile Rest Client injected via CDI.
     * The @RestClient qualifier is mandatory in MicroProfile Rest Client 4.0.
     */
    @Inject
    @RestClient
    private ProductClient productClient;

    /**
     * Retrieves a product by ID from the catalog service.
     * 
     * Demonstrates:
     * - Simple method call on injected REST client
     * - Handling checked exception from ResponseExceptionMapper
     * - Automatic JSON deserialization to Product object
     * - Custom exception handling for 404 responses
     * 
     * @param productId The product ID to retrieve
     * @return The product or null if not found
     */
    public Product getProduct(Long productId) {
        LOGGER.info("Fetching product with ID: " + productId);
        try {
            Product product = productClient.getProductById(productId);
            LOGGER.info("Successfully retrieved product: " + product.getName());
            return product;
        } catch (ProductNotFoundException e) {
            // Checked exception from ResponseExceptionMapper for 404 responses
            LOGGER.warning("Product not found (404): " + e.getMessage());
            return null;
        } catch (ServiceUnavailableException e) {
            // Unchecked exception from ResponseExceptionMapper for 503 responses
            LOGGER.severe("Catalog service unavailable (503): " + e.getMessage());
            throw e; // Re-throw to caller
        } catch (RuntimeException e) {
            // Other runtime exceptions from ResponseExceptionMapper
            LOGGER.severe("Unexpected error: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if a product is available for payment processing.
     * 
     * Demonstrates:
     * - Exception handling with REST clients
     * - Distinguishing between ProductNotFoundException and other errors
     * - Boolean logic based on client response
     * 
     * @param productId The product ID to check
     * @return true if product exists and is available, false otherwise
     */
    public boolean isProductAvailable(Long productId) {
        try {
            Product product = productClient.getProductById(productId);
            boolean available = product != null && product.getPrice() != null && product.getPrice() > 0;
            LOGGER.info("Product " + productId + " availability: " + available);
            return available;
        } catch (ProductNotFoundException e) {
            // Product doesn't exist - return false, don't propagate exception
            LOGGER.info("Product " + productId + " not found (404)");
            return false;
        } catch (Exception e) {
            LOGGER.warning("Error checking product " + productId + " availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieves all products from the catalog service.
     * 
     * Demonstrates:
     * - REST client method returning collections
     * - Handling list responses
     * 
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        LOGGER.info("Fetching all products from catalog service");
        try {
            List<Product> products = productClient.getAllProducts();
            LOGGER.info("Retrieved " + products.size() + " products");
            return products;
        } catch (Exception e) {
            LOGGER.severe("Failed to retrieve products: " + e.getMessage());
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Validates if a product price matches the expected amount.
     * 
     * Demonstrates:
     * - Business logic using REST client data
     * - Validation scenarios in payment processing
     * - Exception handling for not found products
     * 
     * @param productId The product ID
     * @param expectedPrice The expected price
     * @return true if prices match, false otherwise
     */
    public boolean validateProductPrice(Long productId, Double expectedPrice) {
        try {
            Product product = productClient.getProductById(productId);
            if (product != null && product.getPrice() != null) {
                boolean priceMatches = product.getPrice().equals(expectedPrice);
                if (!priceMatches) {
                    LOGGER.warning(String.format(
                        "Price mismatch for product %d: expected %.2f, actual %.2f",
                        productId, expectedPrice, product.getPrice()
                    ));
                }
                return priceMatches;
            }
            return false;
        } catch (Exception e) {
            LOGGER.severe("Failed to validate product price: " + e.getMessage());
            return false;
        }
    }
}
