package io.microprofile.tutorial.store.product.repository;

import io.microprofile.tutorial.store.product.entity.Product;
import java.util.List;

/**
 * Repository interface for Product entity operations.
 * Defines the contract for product data access.
 */
public interface ProductRepositoryInterface {
    
    /**
     * Retrieves all products.
     *
     * @return List of all products
     */
    List<Product> findAllProducts();
    
    /**
     * Retrieves a product by ID.
     *
     * @param id Product ID
     * @return The product or null if not found
     */
    Product findProductById(Long id);
    
    /**
     * Creates a new product.
     *
     * @param product Product data to create
     * @return The created product with ID
     */
    Product createProduct(Product product);
    
    /**
     * Updates an existing product.
     *
     * @param product Updated product data
     * @return The updated product
     */
    Product updateProduct(Product product);
    
    /**
     * Deletes a product by ID.
     *
     * @param id ID of the product to delete
     * @return true if deleted, false if not found
     */
    boolean deleteProduct(Long id);
    
    /**
     * Searches for products by criteria.
     *
     * @param name Product name (optional)
     * @param description Product description (optional)
     * @param minPrice Minimum price (optional)
     * @param maxPrice Maximum price (optional)
     * @return List of matching products
     */
    List<Product> searchProducts(String name, String description, Double minPrice, Double maxPrice);
}
