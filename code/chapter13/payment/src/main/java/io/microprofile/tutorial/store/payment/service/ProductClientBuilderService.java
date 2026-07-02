package io.microprofile.tutorial.store.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.microprofile.tutorial.store.payment.client.ProductClient;
import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service demonstrating programmatic REST client creation using RestClientBuilder.
 * 
 * This class showcases:
 * - Creating REST clients programmatically without CDI injection
 * - Using the RestClientBuilder fluent API
 * - Try-with-resources pattern for automatic resource cleanup
 * - Dynamic configuration from MicroProfile Config
 * - Configuring timeouts and connection parameters
 * - When to use programmatic vs CDI-based client creation
 * 
 * Use RestClientBuilder when:
 * - CDI is unavailable
 * - Client configuration must be determined dynamically at runtime
 * - Multiple endpoints need different configurations
 * - Creating clients in utility methods or batch jobs
 * 
 * Always use try-with-resources to ensure proper resource cleanup.
 */
@ApplicationScoped
public class ProductClientBuilderService {

    private static final Logger LOGGER = Logger.getLogger(ProductClientBuilderService.class.getName());

    /**
     * Checks if a product is available using a programmatically created client.
     * Demonstrates the basic RestClientBuilder usage with try-with-resources.
     * 
     * @param productId The product ID to check
     * @return true if product exists, false otherwise
     */
    public boolean isProductAvailable(Long productId) {
        LOGGER.info("Checking product availability using RestClientBuilder: " + productId);
        
        // MicroProfile Rest Client 4.0 introduces baseUri(String) for convenience
        // No need to call URI.create() explicitly
        try (ProductClient productClient = RestClientBuilder.newBuilder()
                .baseUri("http://localhost:5050/catalog/api")
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build(ProductClient.class)) {

            Product product = productClient.getProductById(productId);
            LOGGER.info("Product found: " + product.getName());
            return product != null;

        } catch (ProductNotFoundException e) {
            LOGGER.log(Level.WARNING, "Product not found with ID: " + productId, e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error checking product availability: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves all products using a programmatically created client.
     * Demonstrates try-with-resources ensuring proper client closure.
     * 
     * @return List of all products, or empty list if error occurs
     */
    public List<Product> getAllProducts() {
        LOGGER.info("Retrieving all products using RestClientBuilder");
        
        try (ProductClient productClient = RestClientBuilder.newBuilder()
                .baseUri("http://localhost:5050/catalog/api")
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build(ProductClient.class)) {

            List<Product> products = productClient.getAllProducts();
            LOGGER.info("Retrieved " + products.size() + " products");
            return products;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving products: " + e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Creates a REST client with dynamic configuration from MicroProfile Config.
     * Demonstrates reading configuration at runtime for flexible deployment.
     * 
     * Configuration properties:
     * - catalog-service/mp-rest/url - Base URL of the catalog service
     * - catalog-service/mp-rest/connectTimeout - Connection timeout in milliseconds
     * - catalog-service/mp-rest/readTimeout - Read timeout in milliseconds
     * 
     * @param productId The product ID to retrieve
     * @return The product, or null if not found
     */
    public Product getProductWithDynamicConfig(Long productId) {
        LOGGER.info("Getting product with dynamic configuration: " + productId);
        
        Config config = ConfigProvider.getConfig();
        
        // Read configuration from MicroProfile Config
        String baseUrl = config.getValue("catalog-service/mp-rest/url", String.class);
        int connectTimeout = config.getOptionalValue("catalog-service/mp-rest/connectTimeout", Integer.class)
                .orElse(3000);
        int readTimeout = config.getOptionalValue("catalog-service/mp-rest/readTimeout", Integer.class)
                .orElse(5000);
        
        LOGGER.info(String.format("Using dynamic config - URL: %s, ConnectTimeout: %dms, ReadTimeout: %dms",
                baseUrl, connectTimeout, readTimeout));
        
        try (ProductClient productClient = RestClientBuilder.newBuilder()
                .baseUri(baseUrl)
                .connectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build(ProductClient.class)) {

            Product product = productClient.getProductById(productId);
            LOGGER.info("Product retrieved with dynamic config: " + product.getName());
            return product;

        } catch (ProductNotFoundException e) {
            LOGGER.log(Level.WARNING, "Product not found: " + productId, e);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving product: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Creates a REST client with custom configuration based on environment.
     * Demonstrates conditional configuration for different deployment scenarios.
     * 
     * @param environment Environment name (e.g., "dev", "staging", "prod")
     * @param productId The product ID to retrieve
     * @return The product, or null if not found
     */
    public Product getProductForEnvironment(String environment, Long productId) {
        LOGGER.info("Getting product for environment: " + environment);
        
        // Determine base URL based on environment
        String baseUrl = switch (environment.toLowerCase()) {
            case "dev" -> "http://localhost:5050/catalog/api";
            case "staging" -> "http://staging-catalog:8080/catalog/api";
            case "prod" -> "http://catalog-service:8080/catalog/api";
            default -> "http://localhost:5050/catalog/api";
        };
        
        // Use shorter timeouts for production
        long timeout = "prod".equals(environment) ? 2000 : 5000;
        
        LOGGER.info(String.format("Using environment config - URL: %s, Timeout: %dms", baseUrl, timeout));
        
        try (ProductClient productClient = RestClientBuilder.newBuilder()
                .baseUri(baseUrl)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .followRedirects(true) // Enable redirect following
                .build(ProductClient.class)) {

            return productClient.getProductById(productId);

        } catch (ProductNotFoundException e) {
            LOGGER.log(Level.WARNING, "Product not found: " + productId, e);
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving product: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Demonstrates creating multiple clients with different configurations.
     * Shows when programmatic creation is preferred over CDI injection.
     * 
     * @param productIds List of product IDs to check
     * @return Number of available products
     */
    public int checkMultipleProducts(List<Long> productIds) {
        LOGGER.info("Checking availability for " + productIds.size() + " products");
        
        int availableCount = 0;
        
        for (Long productId : productIds) {
            // Create a new client for each product check
            // This demonstrates the flexibility of programmatic creation
            try (ProductClient productClient = RestClientBuilder.newBuilder()
                    .baseUri("http://localhost:5050/catalog/api")
                    .connectTimeout(1, TimeUnit.SECONDS) // Shorter timeout for batch operations
                    .readTimeout(2, TimeUnit.SECONDS)
                    .build(ProductClient.class)) {

                productClient.getProductById(productId);
                availableCount++;
                
            } catch (ProductNotFoundException e) {
                LOGGER.fine("Product not found: " + productId);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking product: " + productId, e);
            }
        }
        
        LOGGER.info(String.format("Found %d out of %d products available", 
                availableCount, productIds.size()));
        
        return availableCount;
    }
}
