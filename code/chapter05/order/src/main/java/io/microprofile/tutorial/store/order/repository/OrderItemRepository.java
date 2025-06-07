package io.microprofile.tutorial.store.order.repository;

import io.microprofile.tutorial.store.order.entity.OrderItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple in-memory repository for OrderItem objects.
 * This class provides CRUD operations for OrderItem entities to demonstrate MicroProfile concepts.
 */
@ApplicationScoped
public class OrderItemRepository {

    private final Map<Long, OrderItem> orderItems = new HashMap<>();
    private long nextId = 1;

    /**
     * Saves an order item to the repository.
     * If the order item has no ID, a new ID is assigned.
     *
     * @param orderItem The order item to save
     * @return The saved order item with ID assigned
     */
    public OrderItem save(OrderItem orderItem) {
        if (orderItem.getOrderItemId() == null) {
            orderItem.setOrderItemId(nextId++);
        }
        orderItems.put(orderItem.getOrderItemId(), orderItem);
        return orderItem;
    }

    /**
     * Finds an order item by ID.
     *
     * @param id The order item ID
     * @return An Optional containing the order item if found, or empty if not found
     */
    public Optional<OrderItem> findById(Long id) {
        return Optional.ofNullable(orderItems.get(id));
    }

    /**
     * Finds order items by order ID.
     *
     * @param orderId The order ID
     * @return A list of order items for the specified order
     */
    public List<OrderItem> findByOrderId(Long orderId) {
        return orderItems.values().stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    /**
     * Finds order items by product ID.
     *
     * @param productId The product ID
     * @return A list of order items for the specified product
     */
    public List<OrderItem> findByProductId(Long productId) {
        return orderItems.values().stream()
                .filter(item -> item.getProductId().equals(productId))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all order items from the repository.
     *
     * @return A list of all order items
     */
    public List<OrderItem> findAll() {
        return new ArrayList<>(orderItems.values());
    }

    /**
     * Deletes an order item by ID.
     *
     * @param id The ID of the order item to delete
     * @return true if the order item was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        return orderItems.remove(id) != null;
    }

    /**
     * Deletes all order items for an order.
     *
     * @param orderId The ID of the order
     * @return The number of order items deleted
     */
    public int deleteByOrderId(Long orderId) {
        List<Long> itemsToDelete = orderItems.values().stream()
                .filter(item -> item.getOrderId().equals(orderId))
                .map(OrderItem::getOrderItemId)
                .collect(Collectors.toList());
        
        itemsToDelete.forEach(orderItems::remove);
        return itemsToDelete.size();
    }

    /**
     * Updates an existing order item.
     *
     * @param id The ID of the order item to update
     * @param orderItem The updated order item information
     * @return An Optional containing the updated order item, or empty if not found
     */
    public Optional<OrderItem> update(Long id, OrderItem orderItem) {
        if (!orderItems.containsKey(id)) {
            return Optional.empty();
        }
        
        orderItem.setOrderItemId(id);
        orderItems.put(id, orderItem);
        return Optional.of(orderItem);
    }
}
