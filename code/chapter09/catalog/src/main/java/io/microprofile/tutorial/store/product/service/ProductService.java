package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.JPA;
import io.microprofile.tutorial.store.product.repository.ProductRepositoryInterface;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

/**
 * Service class for Product operations.
 * Contains business logic for product management.
 */
@ApplicationScoped
public class ProductService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    @Inject
    @JPA
    private ProductRepositoryInterface repository;

    /**
     * Retrieves all products.
     *
     * @return List of all products
     */
    public List<Product> findAllProducts() {
        LOGGER.info("Service: Finding all products");
        return repository.findAllProducts();
    }
    
    /**
     * Retrieves a product by ID.
     *
     * @param id Product ID
     * @return The product or null if not found
     */
    @CircuitBreaker(
        requestVolumeThreshold = 10,
        failureRatio = 0.5,
        delay = 5000,
        successThreshold = 2,
        failOn = RuntimeException.class
    )
     public Product findProductById(Long id) {
        LOGGER.info("Service: Finding product with ID: " + id);

        // Logic to call the product details service
        if (Math.random() > 0.7) {
            throw new RuntimeException("Simulated service failure");
        }
        return repository.findProductById(id);
    }
    
    /**
     * Creates a new product.
     *
     * @param product Product data to create
     * @return The created product with ID
     */
    public Product createProduct(Product product) {
        LOGGER.info("Service: Creating new product: " + product);
        return repository.createProduct(product);
    }
    
    /**
     * Updates an existing product.
     *
     * @param id ID of the product to update
     * @param updatedProduct Updated product data
     * @return The updated product or null if not found
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        LOGGER.info("Service: Updating product with ID: " + id);
        
        Product existingProduct = repository.findProductById(id);
        if (existingProduct != null) {
            // Set the ID to ensure correct update
            updatedProduct.setId(id);
            return repository.updateProduct(updatedProduct);
        }
        return null;
    }
    
    /**
     * Deletes a product by ID.
     *
     * @param id ID of the product to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteProduct(Long id) {
        LOGGER.info("Service: Deleting product with ID: " + id);
        return repository.deleteProduct(id);
    }
    
    /**
     * Searches for products by criteria.
     *
     * @param name Product name (optional)
     * @param description Product description (optional)
     * @param minPrice Minimum price (optional)
     * @param maxPrice Maximum price (optional)
     * @return List of matching products
     */
    public List<Product> searchProducts(String name, String description, Double minPrice, Double maxPrice) {
        LOGGER.info("Service: Searching for products with criteria");
        return repository.searchProducts(name, description, minPrice, maxPrice);
    }
}
