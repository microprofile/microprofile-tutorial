package io.microprofile.tutorial.store.order.service;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.OrderItem;
import io.microprofile.tutorial.store.order.entity.OrderStatus;
import io.microprofile.tutorial.store.order.repository.OrderItemRepository;
import io.microprofile.tutorial.store.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service class for Order management operations.
 */
@ApplicationScoped
public class OrderService {

    @Inject
    private OrderRepository orderRepository;
    
    @Inject
    private OrderItemRepository orderItemRepository;

    /**
     * Creates a new order with items.
     *
     * @param order The order to create
     * @return The created order
     */
    @Transactional
    public Order createOrder(Order order) {
        // Set default values
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.CREATED);
        }
        
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        // Calculate total price from order items if not specified
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPriceAtOrder().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalPrice(total);
        }
        
        // Save the order first
        Order savedOrder = orderRepository.save(order);
        
        // Save each order item
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrderId(savedOrder.getOrderId());
                orderItemRepository.save(item);
            }
        }
        
        // Retrieve the complete order with items
        return getOrderById(savedOrder.getOrderId());
    }

    /**
     * Gets an order by ID with its items.
     *
     * @param id The order ID
     * @return The order with its items
     * @throws WebApplicationException if the order is not found
     */
    public Order getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException("Order not found", Response.Status.NOT_FOUND));
        
        // Load order items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        order.setOrderItems(items);
        
        return order;
    }

    /**
     * Gets all orders with their items.
     *
     * @return A list of all orders with their items
     */
    public List<Order> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        
        // Load items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            order.setOrderItems(items);
        }
        
        return orders;
    }

    /**
     * Gets orders by user ID.
     *
     * @param userId The user ID
     * @return A list of orders for the specified user
     */
    public List<Order> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        
        // Load items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            order.setOrderItems(items);
        }
        
        return orders;
    }

    /**
     * Gets orders by status.
     *
     * @param status The order status
     * @return A list of orders with the specified status
     */
    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        
        // Load items for each order
        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
            order.setOrderItems(items);
        }
        
        return orders;
    }

    /**
     * Updates an order.
     *
     * @param id The order ID
     * @param order The updated order information
     * @return The updated order
     * @throws WebApplicationException if the order is not found
     */
    @Transactional
    public Order updateOrder(Long id, Order order) {
        // Check if order exists
        if (!orderRepository.findById(id).isPresent()) {
            throw new WebApplicationException("Order not found", Response.Status.NOT_FOUND);
        }
        
        order.setOrderId(id);
        order.setUpdatedAt(LocalDateTime.now());
        
        // Handle order items if provided
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            // Delete existing items for this order
            orderItemRepository.deleteByOrderId(id);
            
            // Save new items
            for (OrderItem item : order.getOrderItems()) {
                item.setOrderId(id);
                orderItemRepository.save(item);
            }
            
            // Recalculate total price from order items
            BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPriceAtOrder().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            order.setTotalPrice(total);
        }
        
        // Update the order
        Order updatedOrder = orderRepository.update(id, order)
                .orElseThrow(() -> new WebApplicationException("Failed to update order", Response.Status.INTERNAL_SERVER_ERROR));
        
        // Reload items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        updatedOrder.setOrderItems(items);
        
        return updatedOrder;
    }

    /**
     * Updates the status of an order.
     *
     * @param id The order ID
     * @param status The new status
     * @return The updated order
     * @throws WebApplicationException if the order is not found
     */
    public Order updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException("Order not found", Response.Status.NOT_FOUND));
        
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.update(id, order)
                .orElseThrow(() -> new WebApplicationException("Failed to update order status", Response.Status.INTERNAL_SERVER_ERROR));
        
        // Reload items
        List<OrderItem> items = orderItemRepository.findByOrderId(id);
        updatedOrder.setOrderItems(items);
        
        return updatedOrder;
    }

    /**
     * Deletes an order and its items.
     *
     * @param id The order ID
     * @throws WebApplicationException if the order is not found
     */
    @Transactional
    public void deleteOrder(Long id) {
        // Check if order exists
        if (!orderRepository.findById(id).isPresent()) {
            throw new WebApplicationException("Order not found", Response.Status.NOT_FOUND);
        }
        
        // Delete order items first
        orderItemRepository.deleteByOrderId(id);
        
        // Delete the order
        boolean deleted = orderRepository.deleteById(id);
        if (!deleted) {
            throw new WebApplicationException("Failed to delete order", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets an order item by ID.
     *
     * @param id The order item ID
     * @return The order item
     * @throws WebApplicationException if the order item is not found
     */
    public OrderItem getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException("Order item not found", Response.Status.NOT_FOUND));
    }

    /**
     * Gets order items by order ID.
     *
     * @param orderId The order ID
     * @return A list of order items for the specified order
     */
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    /**
     * Adds an item to an order.
     *
     * @param orderId The order ID
     * @param orderItem The order item to add
     * @return The added order item
     * @throws WebApplicationException if the order is not found
     */
    @Transactional
    public OrderItem addOrderItem(Long orderId, OrderItem orderItem) {
        // Check if order exists
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new WebApplicationException("Order not found", Response.Status.NOT_FOUND));
        
        orderItem.setOrderId(orderId);
        OrderItem savedItem = orderItemRepository.save(orderItem);
        
        // Update order total price
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = items.stream()
            .map(item -> item.getPriceAtOrder().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalPrice(total);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(orderId, order);
        
        return savedItem;
    }

    /**
     * Updates an order item.
     *
     * @param itemId The order item ID
     * @param orderItem The updated order item
     * @return The updated order item
     * @throws WebApplicationException if the order item is not found
     */
    @Transactional
    public OrderItem updateOrderItem(Long itemId, OrderItem orderItem) {
        // Check if item exists
        OrderItem existingItem = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new WebApplicationException("Order item not found", Response.Status.NOT_FOUND));
        
        // Keep the same orderId
        orderItem.setOrderItemId(itemId);
        orderItem.setOrderId(existingItem.getOrderId());
        
        OrderItem updatedItem = orderItemRepository.update(itemId, orderItem)
                .orElseThrow(() -> new WebApplicationException("Failed to update order item", Response.Status.INTERNAL_SERVER_ERROR));
        
        // Update order total price
        Long orderId = updatedItem.getOrderId();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new WebApplicationException("Order not found", Response.Status.INTERNAL_SERVER_ERROR));
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = items.stream()
            .map(item -> item.getPriceAtOrder().multiply(new BigDecimal(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalPrice(total);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(orderId, order);
        
        return updatedItem;
    }

    /**
     * Deletes an order item.
     *
     * @param itemId The order item ID
     * @throws WebApplicationException if the order item is not found
     */
    @Transactional
    public void deleteOrderItem(Long itemId) {
        // Check if item exists and get its orderId before deletion
        OrderItem item = orderItemRepository.findById(itemId)
                .orElseThrow(() -> new WebApplicationException("Order item not found", Response.Status.NOT_FOUND));
        
        Long orderId = item.getOrderId();
        
        // Delete the item
        boolean deleted = orderItemRepository.deleteById(itemId);
        if (!deleted) {
            throw new WebApplicationException("Failed to delete order item", Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        // Update order total price
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new WebApplicationException("Order not found", Response.Status.INTERNAL_SERVER_ERROR));
        
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        BigDecimal total = items.stream()
            .map(i -> i.getPriceAtOrder().multiply(new BigDecimal(i.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        order.setTotalPrice(total);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.update(orderId, order);
    }
}
