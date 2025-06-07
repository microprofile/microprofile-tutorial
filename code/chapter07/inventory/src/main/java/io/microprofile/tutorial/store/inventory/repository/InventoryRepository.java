package io.microprofile.tutorial.store.inventory.repository;

import io.microprofile.tutorial.store.inventory.entity.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Thread-safe in-memory repository for Inventory objects.
 * This class provides CRUD operations for Inventory entities to demonstrate MicroProfile concepts.
 */
@ApplicationScoped
public class InventoryRepository {

    private static final Logger LOGGER = Logger.getLogger(InventoryRepository.class.getName());
    
    // Thread-safe map for inventory storage
    private final Map<Long, Inventory> inventories = new ConcurrentHashMap<>();
    
    // Thread-safe ID generator
    private final AtomicLong idGenerator = new AtomicLong(1);
    
    // Secondary index for faster lookups by productId
    private final Map<Long, Long> productToInventoryIndex = new ConcurrentHashMap<>();

    /**
     * Saves an inventory item to the repository.
     * If the inventory has no ID, a new ID is assigned.
     *
     * @param inventory The inventory to save
     * @return The saved inventory with ID assigned
     */
    public Inventory save(Inventory inventory) {
        // Generate ID if not provided
        if (inventory.getInventoryId() == null) {
            inventory.setInventoryId(idGenerator.getAndIncrement());
        } else {
            // Update idGenerator if the provided ID is greater than current
            long nextId = inventory.getInventoryId() + 1;
            while (true) {
                long currentId = idGenerator.get();
                if (nextId <= currentId || idGenerator.compareAndSet(currentId, nextId)) {
                    break;
                }
            }
        }
        
        LOGGER.fine("Saving inventory with ID: " + inventory.getInventoryId());
        
        // Update the inventory and secondary index
        inventories.put(inventory.getInventoryId(), inventory);
        productToInventoryIndex.put(inventory.getProductId(), inventory.getInventoryId());
        
        return inventory;
    }

    /**
     * Finds an inventory item by ID.
     *
     * @param id The inventory ID
     * @return An Optional containing the inventory if found, or empty if not found
     */
    public Optional<Inventory> findById(Long id) {
        if (id == null) {
            LOGGER.warning("Attempted to find inventory with null ID");
            return Optional.empty();
        }
        return Optional.ofNullable(inventories.get(id));
    }

    /**
     * Finds inventory by product ID.
     *
     * @param productId The product ID
     * @return An Optional containing the inventory if found, or empty if not found
     */
    public Optional<Inventory> findByProductId(Long productId) {
        if (productId == null) {
            LOGGER.warning("Attempted to find inventory with null product ID");
            return Optional.empty();
        }
        
        // Use the secondary index for efficient lookup
        Long inventoryId = productToInventoryIndex.get(productId);
        if (inventoryId != null) {
            return Optional.ofNullable(inventories.get(inventoryId));
        }
        
        // Fall back to scanning if not found in index (ensures consistency)
        return inventories.values().stream()
                .filter(inventory -> productId.equals(inventory.getProductId()))
                .findFirst();
    }

    /**
     * Retrieves all inventory items from the repository.
     *
     * @return A list of all inventory items
     */
    public List<Inventory> findAll() {
        return new ArrayList<>(inventories.values());
    }

    /**
     * Deletes an inventory item by ID.
     *
     * @param id The ID of the inventory to delete
     * @return true if the inventory was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        if (id == null) {
            LOGGER.warning("Attempted to delete inventory with null ID");
            return false;
        }
        
        Inventory removed = inventories.remove(id);
        if (removed != null) {
            // Also remove from the secondary index
            productToInventoryIndex.remove(removed.getProductId());
            LOGGER.fine("Deleted inventory with ID: " + id);
            return true;
        }
        
        LOGGER.fine("Failed to delete inventory with ID (not found): " + id);
        return false;
    }

    /**
     * Updates an existing inventory item.
     *
     * @param id The ID of the inventory to update
     * @param inventory The updated inventory information
     * @return An Optional containing the updated inventory, or empty if not found
     */
    public Optional<Inventory> update(Long id, Inventory inventory) {
        if (id == null || inventory == null) {
            LOGGER.warning("Attempted to update inventory with null ID or null inventory");
            return Optional.empty();
        }
        
        if (!inventories.containsKey(id)) {
            LOGGER.fine("Failed to update inventory with ID (not found): " + id);
            return Optional.empty();
        }
        
        // Get the existing inventory to update its product index if needed
        Inventory existing = inventories.get(id);
        if (existing != null && !existing.getProductId().equals(inventory.getProductId())) {
            // Product ID changed, update the index
            productToInventoryIndex.remove(existing.getProductId());
        }
        
        // Set ID and update the repository
        inventory.setInventoryId(id);
        inventories.put(id, inventory);
        productToInventoryIndex.put(inventory.getProductId(), id);
        
        LOGGER.fine("Updated inventory with ID: " + id);
        return Optional.of(inventory);
    }
}
