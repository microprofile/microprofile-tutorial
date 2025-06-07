package io.microprofile.tutorial.store.inventory.service;

import io.microprofile.tutorial.store.inventory.entity.Inventory;
import io.microprofile.tutorial.store.inventory.exception.InventoryConflictException;
import io.microprofile.tutorial.store.inventory.exception.InventoryNotFoundException;
import io.microprofile.tutorial.store.inventory.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.transaction.Transactional;

/**
 * Service class for Inventory management operations.
 */
@ApplicationScoped
public class InventoryService {

    private static final Logger LOGGER = Logger.getLogger(InventoryService.class.getName());

    @Inject
    private InventoryRepository inventoryRepository;

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
        
        // Validate for conflicts
        for (Inventory inventory : inventories) {
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
}
