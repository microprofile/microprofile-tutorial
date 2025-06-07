package io.microprofile.tutorial.store.product.repository;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Repository class for Product entity.
 * Provides in-memory persistence operations using ConcurrentHashMap.
 */
@ApplicationScoped
public class ProductRepository {
    
    private static final Logger LOGGER = Logger.getLogger(ProductRepository.class.getName());
    
    // In-memory storage using ConcurrentHashMap for thread safety
    private final Map<Long, Product> productsMap = new ConcurrentHashMap<>();
    
    // ID generator
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    /**
     * Constructor with sample data initialization.
     */
    public ProductRepository() {
        // Initialize with sample products
        createProduct(new Product(null, "iPhone", "Apple iPhone 15", 999.99));
        createProduct(new Product(null, "MacBook", "Apple MacBook Air", 1299.0));
        createProduct(new Product(null, "iPad", "Apple iPad Pro", 799.0));
        LOGGER.info("ProductRepository initialized with sample products");
    }
    
    /**
     * Retrieves all products.
     *
     * @return List of all products
     */
    public List<Product> findAllProducts() {
        LOGGER.fine("Repository: Finding all products");
        return new ArrayList<>(productsMap.values());
    }
    
    /**
     * Retrieves a product by ID.
     *
     * @param id Product ID
     * @return The product or null if not found
     */
    public Product findProductById(Long id) {
        LOGGER.fine("Repository: Finding product with ID: " + id);
        return productsMap.get(id);
    }
    
    /**
     * Creates a new product.
     *
     * @param product Product data to create
     * @return The created product with ID
     */
    public Product createProduct(Product product) {
        // Generate ID if not provided
        if (product.getId() == null) {
            product.setId(idGenerator.getAndIncrement());
        } else {
            // Update idGenerator if the provided ID is greater than current
            long nextId = product.getId() + 1;
            while (true) {
                long currentId = idGenerator.get();
                if (nextId <= currentId || idGenerator.compareAndSet(currentId, nextId)) {
                    break;
                }
            }
        }
        
        LOGGER.fine("Repository: Creating product with ID: " + product.getId());
        productsMap.put(product.getId(), product);
        return product;
    }
    
    /**
     * Updates an existing product.
     *
     * @param product Updated product data
     * @return The updated product or null if not found
     */
    public Product updateProduct(Product product) {
        Long id = product.getId();
        if (id != null && productsMap.containsKey(id)) {
            LOGGER.fine("Repository: Updating product with ID: " + id);
            productsMap.put(id, product);
            return product;
        }
        LOGGER.warning("Repository: Product not found for update, ID: " + id);
        return null;
    }
    
    /**
     * Deletes a product by ID.
     *
     * @param id ID of the product to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteProduct(Long id) {
        if (productsMap.containsKey(id)) {
            LOGGER.fine("Repository: Deleting product with ID: " + id);
            productsMap.remove(id);
            return true;
        }
        LOGGER.warning("Repository: Product not found for deletion, ID: " + id);
        return false;
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
        LOGGER.fine("Repository: Searching for products with criteria");
        
        return productsMap.values().stream()
            .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
            .filter(p -> description == null || p.getDescription().toLowerCase().contains(description.toLowerCase()))
            .filter(p -> minPrice == null || p.getPrice() >= minPrice)
            .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
            .collect(Collectors.toList());
    }
}