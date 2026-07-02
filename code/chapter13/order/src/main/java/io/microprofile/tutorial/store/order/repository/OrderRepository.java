package io.microprofile.tutorial.store.order.repository;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.OrderStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple in-memory repository for Order objects.
 * This class provides CRUD operations for Order entities to demonstrate MicroProfile concepts.
 */
@ApplicationScoped
public class OrderRepository {

    private final Map<Long, Order> orders = new HashMap<>();
    private long nextId = 1;

    /**
     * Saves an order to the repository.
     * If the order has no ID, a new ID is assigned.
     *
     * @param order The order to save
     * @return The saved order with ID assigned
     */
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(nextId++);
        }
        orders.put(order.getOrderId(), order);
        return order;
    }

    /**
     * Finds an order by ID.
     *
     * @param id The order ID
     * @return An Optional containing the order if found, or empty if not found
     */
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(orders.get(id));
    }

    /**
     * Finds orders by user ID.
     *
     * @param userId The user ID
     * @return A list of orders for the specified user
     */
    public List<Order> findByUserId(Long userId) {
        return orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Finds orders by status.
     *
     * @param status The order status
     * @return A list of orders with the specified status
     */
    public List<Order> findByStatus(OrderStatus status) {
        return orders.values().stream()
                .filter(order -> order.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all orders from the repository.
     *
     * @return A list of all orders
     */
    public List<Order> findAll() {
        return new ArrayList<>(orders.values());
    }

    /**
     * Deletes an order by ID.
     *
     * @param id The ID of the order to delete
     * @return true if the order was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        return orders.remove(id) != null;
    }

    /**
     * Updates an existing order.
     *
     * @param id The ID of the order to update
     * @param order The updated order information
     * @return An Optional containing the updated order, or empty if not found
     */
    public Optional<Order> update(Long id, Order order) {
        if (!orders.containsKey(id)) {
            return Optional.empty();
        }
        
        order.setOrderId(id);
        orders.put(id, order);
        return Optional.of(order);
    }
}
