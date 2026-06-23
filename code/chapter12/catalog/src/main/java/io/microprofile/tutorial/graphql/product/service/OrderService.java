package io.microprofile.tutorial.graphql.product.service;

import io.microprofile.tutorial.graphql.product.entity.Order;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing orders
 */
@ApplicationScoped
public class OrderService {
    
    private final Map<Long, Order> orders = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    @Inject
    InventoryService inventoryService;
    
    /**
     * Create a new order
     * 
     * @param productId The product ID to order
     * @param quantity The quantity to order
     * @return The created order
     */
    public Order createOrder(Long productId, int quantity) {
        Long orderId = idCounter.getAndIncrement();
        Order order = new Order(
            orderId,
            productId,
            quantity,
            "PENDING",
            LocalDateTime.now()
        );
        orders.put(orderId, order);
        
        // Deduct stock
        inventoryService.deductStock(productId, quantity);
        
        return order;
    }
    
    /**
     * Get order by ID
     * 
     * @param id The order ID
     * @return The order or null if not found
     */
    public Order findById(Long id) {
        return orders.get(id);
    }
}
