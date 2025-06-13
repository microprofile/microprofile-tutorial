package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.client.ProductClientJson;
import io.microprofile.tutorial.store.payment.dto.product.Product;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for integrating with the Product/Catalog service.
 * Provides business logic for product validation and retrieval in the context of payment processing.
 */
@ApplicationScoped
public class ProductIntegrationService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductIntegrationService.class.getName());

    @Inject
    @ConfigProperty(name = "catalog.service.url", defaultValue = "http://localhost:5050/catalog/api/products")
    private String catalogServiceUrl;

    /**
     * Validates if a product is suitable for payment processing.
     * 
     * @param productId The product ID to validate
     * @return true if the product is valid for payment, false otherwise
     */
    public boolean validateProductForPayment(Long productId) {
        LOGGER.info("Validating product for payment: " + productId);
        
        try {
            Product product = getProductDetails(productId);
            if (product == null) {
                LOGGER.warning("Product not found: " + productId);
                return false;
            }
            
            // Basic validation: product must have a valid price and name
            boolean isValid = product.price != null && 
                             product.price > 0.0 &&
                             product.name != null && 
                             !product.name.trim().isEmpty();
            
            LOGGER.info("Product " + productId + " validation result: " + isValid);
            return isValid;
            
        } catch (Exception e) {
            LOGGER.severe("Error validating product " + productId + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets detailed information about a specific product.
     * 
     * @param productId The product ID
     * @return Product details or null if not found
     */
    public Product getProductDetails(Long productId) {
        LOGGER.info("Fetching product details for ID: " + productId);
        
        try {
            Product[] allProducts = ProductClientJson.getProductsWithJsonp(catalogServiceUrl);
            
            if (allProducts != null) {
                for (Product product : allProducts) {
                    if (product.id.equals(productId)) {
                        LOGGER.info("Found product: " + product.name + " (ID: " + productId + ")");
                        return product;
                    }
                }
            }
            
            LOGGER.warning("Product not found with ID: " + productId);
            return null;
            
        } catch (Exception e) {
            LOGGER.severe("Error fetching product details for ID " + productId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets products within a specified price range.
     * 
     * @param minPrice Minimum price (inclusive)
     * @param maxPrice Maximum price (inclusive)
     * @return List of products within the price range
     */
    public List<Product> getProductsByPriceRange(double minPrice, double maxPrice) {
        LOGGER.info("Fetching products in price range: " + minPrice + " - " + maxPrice);
        
        try {
            Product[] allProducts = ProductClientJson.getProductsWithJsonp(catalogServiceUrl);
            
            if (allProducts == null) {
                LOGGER.warning("No products returned from catalog service");
                return new ArrayList<>();
            }
            
            List<Product> filteredProducts = Arrays.stream(allProducts)
                .filter(product -> product.price != null)
                .filter(product -> product.price >= minPrice)
                .filter(product -> product.price <= maxPrice)
                .collect(Collectors.toList());
            
            LOGGER.info("Found " + filteredProducts.size() + " products in price range");
            return filteredProducts;
            
        } catch (Exception e) {
            LOGGER.severe("Error fetching products by price range: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Gets all available products.
     * 
     * @return Array of all products
     */
    public Product[] getAllProducts() {
        LOGGER.info("Fetching all products");
        
        try {
            Product[] products = ProductClientJson.getProductsWithJsonp(catalogServiceUrl);
            
            if (products != null) {
                LOGGER.info("Retrieved " + products.length + " products");
            } else {
                LOGGER.warning("No products returned from catalog service");
                products = new Product[0];
            }
            
            return products;
            
        } catch (Exception e) {
            LOGGER.severe("Error fetching all products: " + e.getMessage());
            return new Product[0];
        }
    }
}
