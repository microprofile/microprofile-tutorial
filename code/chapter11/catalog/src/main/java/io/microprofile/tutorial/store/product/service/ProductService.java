package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for Product operations.
 * Contains business logic for product management.
 */
@RequestScoped
public class ProductService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    @Inject
    private ProductRepository repository;

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
    public Product findProductById(Long id) {
        LOGGER.info("Service: Finding product with ID: " + id);
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
