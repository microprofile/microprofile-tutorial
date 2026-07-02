package io.microprofile.tutorial.store.payment.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.microprofile.tutorial.store.payment.client.ProductClientWithFilters;
import io.microprofile.tutorial.store.payment.dto.product.Product;
import io.microprofile.tutorial.store.payment.exception.ProductNotFoundException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service demonstrating MicroProfile Rest Client with custom filters.
 * 
 * This service injects ProductClientWithFilters which has multiple filters registered:
 * - BearerTokenFilter: Adds authentication headers
 * - CorrelationIdFilter: Adds distributed tracing headers
 * - RequestLoggingFilter: Logs outgoing requests
 * - ResponseLoggingFilter: Logs incoming responses
 * 
 * Compare the logs from this service with ProductCatalogService (no filters)
 * to see the difference in observability and debugging capabilities.
 * 
 * The filters provide:
 * - Authentication without modifying business logic
 * - Distributed tracing across microservices
 * - Comprehensive request/response logging
 * - Header propagation patterns
 */
@ApplicationScoped
public class FilteredProductCatalogService {

    private static final Logger LOGGER = Logger.getLogger(FilteredProductCatalogService.class.getName());

    @Inject
    @RestClient
    ProductClientWithFilters filteredClient;

    /**
     * Gets all products using filtered REST client.
     * 
     * Watch the logs to see filter execution:
     * 1. BearerTokenFilter adds Authorization header
     * 2. CorrelationIdFilter adds X-Correlation-ID
     * 3. RequestLoggingFilter logs complete request
     * 4. HTTP request sent
     * 5. HTTP response received
     * 6. ResponseLoggingFilter logs complete response
     * 
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        LOGGER.info("Getting all products using filtered REST client");
        
        try {
            List<Product> products = filteredClient.getAllProducts();
            LOGGER.info("Successfully retrieved " + products.size() + " products with filters");
            return products;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving products with filters: " + e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products", e);
        }
    }

    /**
     * Gets a specific product by ID using filtered REST client.
     * 
     * Demonstrates filter execution with path parameters.
     * Check logs to see how filters handle parameterized requests.
     * 
     * @param productId The product ID
     * @return The product
     * @throws ProductNotFoundException if product not found
     */
    public Product getProduct(Long productId) throws ProductNotFoundException {
        LOGGER.info("Getting product with filters: " + productId);
        
        try {
            Product product = filteredClient.getProductById(productId);
            LOGGER.info("Successfully retrieved product with filters: " + product.getName());
            return product;
            
        } catch (ProductNotFoundException e) {
            LOGGER.log(Level.WARNING, "Product not found with filters: " + productId, e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error retrieving product with filters: " + e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve product", e);
        }
    }

    /**
     * Checks if a product is available using filtered REST client.
     * 
     * Demonstrates how filters work with exception handling.
     * The 404 response will still be logged by ResponseLoggingFilter
     * before the exception is thrown.
     * 
     * @param productId The product ID to check
     * @return true if product is available, false otherwise
     */
    public boolean isProductAvailable(Long productId) {
        LOGGER.info("Checking product availability with filters: " + productId);
        
        try {
            Product product = filteredClient.getProductById(productId);
            boolean available = (product != null);
            LOGGER.info("Product " + productId + " availability with filters: " + available);
            return available;
            
        } catch (ProductNotFoundException e) {
            LOGGER.fine("Product not found with filters (expected for availability check): " + productId);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking availability with filters: " + e.getMessage(), e);
            return false;
        }
    }
}
