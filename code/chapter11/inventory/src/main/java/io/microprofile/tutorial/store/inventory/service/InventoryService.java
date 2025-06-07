package io.microprofile.tutorial.store.inventory.service;

import io.microprofile.tutorial.store.inventory.entity.Inventory;
import io.microprofile.tutorial.store.inventory.exception.InventoryConflictException;
import io.microprofile.tutorial.store.inventory.exception.InventoryNotFoundException;
import io.microprofile.tutorial.store.inventory.repository.InventoryRepository;
import io.microprofile.tutorial.store.inventory.client.ProductServiceClient;
import io.microprofile.tutorial.store.inventory.dto.Product;
import io.microprofile.tutorial.store.inventory.dto.InventoryWithProductInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service class for Inventory management operations.
 */
@ApplicationScoped
public class InventoryService {

    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());

    @Inject
    private InventoryRepository inventoryRepository;

    @Inject
    @RestClient
    private ProductServiceClient productServiceClient;

    /**
     * Checks if a product is available in the catalog service.
     * This method demonstrates the use of RestClientBuilder for programmatic REST client creation.
     * This is a lightweight check that returns only a boolean result.
     *
     * @param productId The product ID to check
     * @return true if the product exists, false otherwise
     */
    public boolean isProductAvailable(Long productId) {
        LOGGER.fine("Checking product availability for ID: " + productId);
        
        try {
            // Demonstrate RestClientBuilder usage - build a REST client programmatically
            URI catalogServiceUri = URI.create("http://localhost:5050/catalog/api");
            
            ProductServiceClient dynamicClient = RestClientBuilder.newBuilder()
                    .baseUri(catalogServiceUri)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build(ProductServiceClient.class);
            
            LOGGER.fine("Built dynamic REST client for catalog service at: " + catalogServiceUri);
            
            Product product = dynamicClient.getProductById(productId);
            boolean available = product != null;
            LOGGER.fine("Product " + productId + " availability check via RestClientBuilder: " + available);
            return available;
            
        } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() == 404) {
                LOGGER.fine("Product " + productId + " not found in catalog (via RestClientBuilder)");
                return false;
            }
            LOGGER.warning("Error checking product availability for ID " + productId + " via RestClientBuilder: " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error checking product availability for ID " + productId + " via RestClientBuilder", e);
            return false;
        }
    }

    /**
     * Validates that a product exists in the catalog service.
     *
     * @param productId The product ID to validate
     * @return The product details if found
     * @throws InventoryNotFoundException if the product is not found in the catalog
     */
    private Product validateProductExists(Long productId) {
        LOGGER.fine("Validating product existence for ID: " + productId);
        
        try {
            Product product = productServiceClient.getProductById(productId);
            if (product == null) {
                throw new InventoryNotFoundException("Product not found in catalog with ID: " + productId);
            }
            LOGGER.fine("Product validated successfully: " + product.getName());
            return product;
        } catch (WebApplicationException e) {
            LOGGER.warning("Product validation failed for ID " + productId + ": " + e.getMessage());
            if (e.getResponse().getStatus() == 404) {
                throw new InventoryNotFoundException("Product not found in catalog with ID: " + productId);
            }
            throw new RuntimeException("Failed to validate product with catalog service: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error validating product " + productId, e);
            throw new RuntimeException("Failed to validate product with catalog service: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new inventory item.
     *
     * @param inventory The inventory to create
     * @return The created inventory
     * @throws InventoryConflictException if inventory with the product ID already exists
     */
    @Transactional
    public Inventory createInventory(Inventory inventory) {
        LOGGER.info("Creating inventory for product ID: " + inventory.getProductId());
        
        // Validate that the product exists in the catalog service
        Product product = validateProductExists(inventory.getProductId());
        LOGGER.info("Product validated: " + product.getName() + " (Price: $" + product.getPrice() + ")");
        
        // Check if product ID already exists
        Optional<Inventory> existingInventory = inventoryRepository.findByProductId(inventory.getProductId());
        if (existingInventory.isPresent()) {
            LOGGER.warning("Conflict: Inventory already exists for product ID: " + inventory.getProductId());
            throw new InventoryConflictException("Inventory for product already exists", Response.Status.CONFLICT);
        }
        
        Inventory result = inventoryRepository.save(inventory);
        LOGGER.info("Created inventory ID: " + result.getInventoryId() + " for product ID: " + result.getProductId());
        return result;
    }

    /**
     * Creates new inventory items in bulk.
     *
     * @param inventories The list of inventories to create
     * @return The list of created inventories
     * @throws InventoryConflictException if any inventory with the same product ID already exists
     */
    @Transactional
    public List<Inventory> createBulkInventories(List<Inventory> inventories) {
        LOGGER.info("Creating bulk inventories: " + inventories.size() + " items");
        
        // Validate products exist in catalog and check for conflicts
        for (Inventory inventory : inventories) {
            // Validate product exists in catalog
            Product product = validateProductExists(inventory.getProductId());
            LOGGER.fine("Product validated for bulk create: " + product.getName() + " (ID: " + inventory.getProductId() + ")");
            
            // Check for existing inventory records
            Optional<Inventory> existingInventory = inventoryRepository.findByProductId(inventory.getProductId());
            if (existingInventory.isPresent()) {
                LOGGER.warning("Conflict detected during bulk create for product ID: " + inventory.getProductId());
                throw new InventoryConflictException("Inventory for product already exists: " + inventory.getProductId());
            }
        }
        
        // Save all inventories
        List<Inventory> created = new ArrayList<>();
        for (Inventory inventory : inventories) {
            created.add(inventoryRepository.save(inventory));
        }
        
        LOGGER.info("Successfully created " + created.size() + " inventory items");
        return created;
    }

    /**
     * Gets an inventory item by ID.
     *
     * @param id The inventory ID
     * @return The inventory
     * @throws InventoryNotFoundException if the inventory is not found
     */
    public Inventory getInventoryById(Long id) {
        LOGGER.fine("Getting inventory by ID: " + id);
        return inventoryRepository.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warning("Inventory not found with ID: " + id);
                    return new InventoryNotFoundException("Inventory not found with ID: " + id);
                });
    }

    /**
     * Gets inventory by product ID.
     *
     * @param productId The product ID
     * @return The inventory
     * @throws InventoryNotFoundException if the inventory is not found
     */
    public Inventory getInventoryByProductId(Long productId) {
        LOGGER.fine("Getting inventory by product ID: " + productId);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> {
                    LOGGER.warning("Inventory not found for product ID: " + productId);
                    return new InventoryNotFoundException("Inventory not found for product", Response.Status.NOT_FOUND);
                });
    }

    /**
     * Gets all inventory items.
     *
     * @return A list of all inventory items
     */
    public List<Inventory> getAllInventories() {
        LOGGER.fine("Getting all inventory items");
        return inventoryRepository.findAll();
    }
    
    /**
     * Gets inventory items with pagination and filtering.
     *
     * @param page Page number (zero-based)
     * @param size Page size
     * @param minQuantity Minimum quantity filter (optional)
     * @param maxQuantity Maximum quantity filter (optional)
     * @return A filtered and paginated list of inventory items
     */
    public List<Inventory> getAllInventories(int page, int size, Integer minQuantity, Integer maxQuantity) {
        LOGGER.fine("Getting inventory items with pagination: page=" + page + ", size=" + size + 
                   ", minQuantity=" + minQuantity + ", maxQuantity=" + maxQuantity);
        
        // First, get all inventories
        List<Inventory> allInventories = inventoryRepository.findAll();
        
        // Apply filters if provided
        List<Inventory> filteredInventories = allInventories.stream()
            .filter(inv -> minQuantity == null || inv.getQuantity() >= minQuantity)
            .filter(inv -> maxQuantity == null || inv.getQuantity() <= maxQuantity)
            .collect(Collectors.toList());
        
        // Apply pagination
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, filteredInventories.size());
        
        // Check if the start index is valid
        if (startIndex >= filteredInventories.size()) {
            return new ArrayList<>();
        }
        
        return filteredInventories.subList(startIndex, endIndex);
    }
    
    /**
     * Counts inventory items with filtering.
     *
     * @param minQuantity Minimum quantity filter (optional)
     * @param maxQuantity Maximum quantity filter (optional)
     * @return The count of inventory items that match the filters
     */
    public long countInventories(Integer minQuantity, Integer maxQuantity) {
        LOGGER.fine("Counting inventory items with filters: minQuantity=" + minQuantity + 
                    ", maxQuantity=" + maxQuantity);
        
        List<Inventory> allInventories = inventoryRepository.findAll();
        
        // Apply filters and count
        return allInventories.stream()
            .filter(inv -> minQuantity == null || inv.getQuantity() >= minQuantity)
            .filter(inv -> maxQuantity == null || inv.getQuantity() <= maxQuantity)
            .count();
    }

    /**
     * Updates an inventory item.
     *
     * @param id The inventory ID
     * @param inventory The updated inventory information
     * @return The updated inventory
     * @throws InventoryNotFoundException if the inventory is not found
     * @throws InventoryConflictException if another inventory with the same product ID exists
     */
    @Transactional
    public Inventory updateInventory(Long id, Inventory inventory) {
        LOGGER.info("Updating inventory ID: " + id + " for product ID: " + inventory.getProductId());
        
        // Validate that the product exists in the catalog service
        Product product = validateProductExists(inventory.getProductId());
        LOGGER.info("Product validated for update: " + product.getName() + " (ID: " + product.getId() + ")");
        
        // Check if product ID exists in a different inventory record
        Optional<Inventory> existingInventoryWithProductId = inventoryRepository.findByProductId(inventory.getProductId());
        if (existingInventoryWithProductId.isPresent() && 
            !existingInventoryWithProductId.get().getInventoryId().equals(id)) {
            LOGGER.warning("Conflict: Another inventory record exists for product ID: " + inventory.getProductId());
            throw new InventoryConflictException("Another inventory record already exists for this product", 
                                             Response.Status.CONFLICT);
        }
        
        return inventoryRepository.update(id, inventory)
                .orElseThrow(() -> {
                    LOGGER.warning("Inventory not found with ID: " + id);
                    return new InventoryNotFoundException("Inventory not found", Response.Status.NOT_FOUND);
                });
    }

    /**
     * Deletes an inventory item.
     *
     * @param id The inventory ID
     * @throws InventoryNotFoundException if the inventory is not found
     */
    @Transactional
    public void deleteInventory(Long id) {
        LOGGER.info("Deleting inventory with ID: " + id);
        boolean deleted = inventoryRepository.deleteById(id);
        if (!deleted) {
            LOGGER.warning("Inventory not found with ID: " + id);
            throw new InventoryNotFoundException("Inventory not found", Response.Status.NOT_FOUND);
        }
        LOGGER.info("Successfully deleted inventory with ID: " + id);
    }

    /**
     * Updates the quantity for a product.
     *
     * @param productId The product ID
     * @param quantity The new quantity
     * @return The updated inventory
     * @throws InventoryNotFoundException if the inventory is not found
     */
    @Transactional
    public Inventory updateQuantity(Long productId, int quantity) {
        if (quantity < 0) {
            LOGGER.warning("Invalid quantity: " + quantity + " for product ID: " + productId);
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        
        LOGGER.info("Updating quantity to " + quantity + " for product ID: " + productId);
        Inventory inventory = getInventoryByProductId(productId);
        int oldQuantity = inventory.getQuantity();
        inventory.setQuantity(quantity);
        
        Inventory updated = inventoryRepository.save(inventory);
        LOGGER.info("Updated quantity from " + oldQuantity + " to " + quantity + 
                   " for product ID: " + productId + " (inventory ID: " + inventory.getInventoryId() + ")");
        
        return updated;
    }

    /**
     * Gets product information for an inventory item.
     *
     * @param inventory The inventory item
     * @return The product details
     */
    public Product getProductInfo(Inventory inventory) {
        return validateProductExists(inventory.getProductId());
    }

    /**
     * Gets inventory with enriched product information.
     *
     * @param inventoryId The inventory ID
     * @return Inventory with product details
     */
    public InventoryWithProductInfo getInventoryWithProductInfo(Long inventoryId) {
        Inventory inventory = getInventoryById(inventoryId);
        Product product = validateProductExists(inventory.getProductId());
        
        return new InventoryWithProductInfo(inventory, product);
    }

    /**
     * Gets all inventories with product information for a specific category.
     *
     * @param category The product category
     * @return List of inventories for products in the specified category
     */
    public List<InventoryWithProductInfo> getInventoriesByCategory(String category) {
        LOGGER.info("Getting inventories for category: " + category);
        
        try {
            // Get products by category from catalog service
            List<Product> productsInCategory = productServiceClient.getProductsByCategory(category);
            
            if (productsInCategory == null || productsInCategory.isEmpty()) {
                LOGGER.info("No products found in category: " + category);
                return new ArrayList<>();
            }
            
            // Find inventories for these products
            List<InventoryWithProductInfo> result = new ArrayList<>();
            for (Product product : productsInCategory) {
                try {
                    Inventory inventory = inventoryRepository.findByProductId(product.getId()).orElse(null);
                    if (inventory != null) {
                        result.add(new InventoryWithProductInfo(inventory, product));
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error getting inventory for product " + product.getId() + ": " + e.getMessage());
                }
            }
            
            LOGGER.info("Found " + result.size() + " inventory items for category: " + category);
            return result;
            
        } catch (WebApplicationException e) {
            LOGGER.warning("Failed to get products by category from catalog service: " + e.getMessage());
            throw new RuntimeException("Failed to retrieve products by category: " + e.getMessage(), e);
        }
    }

    /**
     * Reserves inventory for a product if it's available in the catalog.
     * This method uses isProductAvailable for a lightweight check before reservation.
     *
     * @param productId The product ID
     * @param quantityToReserve The quantity to reserve
     * @return The updated inventory after reservation
     * @throws InventoryNotFoundException if the inventory or product is not found
     * @throws IllegalArgumentException if there's insufficient inventory
     */
    @Transactional
    public Inventory reserveInventory(Long productId, int quantityToReserve) {
        if (quantityToReserve <= 0) {
            throw new IllegalArgumentException("Quantity to reserve must be positive");
        }
        
        LOGGER.info("Attempting to reserve " + quantityToReserve + " units for product ID: " + productId);
        
        // Use isProductAvailable for a lightweight availability check
        if (!isProductAvailable(productId)) {
            LOGGER.warning("Cannot reserve inventory - product " + productId + " is not available in catalog");
            throw new InventoryNotFoundException("Product is not available in catalog: " + productId);
        }
        
        // Get the current inventory
        Inventory inventory = getInventoryByProductId(productId);
        
        // Check if we have enough inventory to reserve
        int availableQuantity = inventory.getQuantity() - inventory.getReservedQuantity();
        if (availableQuantity < quantityToReserve) {
            LOGGER.warning("Insufficient inventory to reserve " + quantityToReserve + 
                          " units for product " + productId + ". Available: " + availableQuantity);
            throw new IllegalArgumentException("Insufficient inventory available. Requested: " + 
                                             quantityToReserve + ", Available: " + availableQuantity);
        }
        
        // Update reserved quantity
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantityToReserve);
        
        Inventory updated = inventoryRepository.save(inventory);
        LOGGER.info("Reserved " + quantityToReserve + " units for product " + productId + 
                   ". New reserved quantity: " + updated.getReservedQuantity());
        
        return updated;
    }

    /**
     * Demonstrates advanced RestClientBuilder usage with custom configuration.
     * This method builds a REST client with specific timeout and error handling settings.
     *
     * @param productId The product ID to check
     * @return Product details if found, null otherwise
     */
    public Product getProductWithCustomClient(Long productId) {
        LOGGER.info("Getting product details using custom RestClientBuilder for ID: " + productId);
        
        try {
            // Build REST client with custom configuration
            URI catalogServiceUri = URI.create("http://localhost:5050/catalog/api");
            
            ProductServiceClient customClient = RestClientBuilder.newBuilder()
                    .baseUri(catalogServiceUri)
                    .connectTimeout(3, TimeUnit.SECONDS)      // Custom connect timeout
                    .readTimeout(8, TimeUnit.SECONDS)         // Custom read timeout
                    .build(ProductServiceClient.class);
            
            LOGGER.info("Built custom REST client with 3s connect and 8s read timeout");
            
            Product product = customClient.getProductById(productId);
            LOGGER.info("Retrieved product via custom client: " + (product != null ? product.getName() : "null"));
            return product;
            
        } catch (WebApplicationException e) {
            LOGGER.warning("WebApplicationException from custom client for product " + productId + 
                          ": Status=" + e.getResponse().getStatus() + ", Message=" + e.getMessage());
            return null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Unexpected error from custom REST client for product " + productId, e);
            return null;
        }
    }
}
