package io.microprofile.tutorial.store.shoppingcart.repository;

import io.microprofile.tutorial.store.shoppingcart.entity.CartItem;
import io.microprofile.tutorial.store.shoppingcart.entity.ShoppingCart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple in-memory repository for ShoppingCart objects.
 * This class provides operations for shopping cart management.
 */
@ApplicationScoped
public class ShoppingCartRepository {

    private final Map<Long, ShoppingCart> carts = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, CartItem>> cartItems = new ConcurrentHashMap<>();
    private long nextCartId = 1;
    private long nextItemId = 1;

    /**
     * Finds a shopping cart by user ID.
     *
     * @param userId The user ID
     * @return An Optional containing the shopping cart if found, or empty if not found
     */
    public Optional<ShoppingCart> findByUserId(Long userId) {
        return carts.values().stream()
                .filter(cart -> cart.getUserId().equals(userId))
                .findFirst();
    }

    /**
     * Finds a shopping cart by cart ID.
     *
     * @param cartId The cart ID
     * @return An Optional containing the shopping cart if found, or empty if not found
     */
    public Optional<ShoppingCart> findById(Long cartId) {
        return Optional.ofNullable(carts.get(cartId));
    }

    /**
     * Creates a new shopping cart for a user.
     *
     * @param userId The user ID
     * @return The created shopping cart
     */
    public ShoppingCart createCart(Long userId) {
        ShoppingCart cart = ShoppingCart.builder()
                .cartId(nextCartId++)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
        carts.put(cart.getCartId(), cart);
        cartItems.put(cart.getCartId(), new HashMap<>());
        
        return cart;
    }

    /**
     * Adds an item to a shopping cart.
     * If the product already exists in the cart, the quantity is increased.
     *
     * @param cartId The cart ID
     * @param item The item to add
     * @return The updated cart item
     */
    public CartItem addItem(Long cartId, CartItem item) {
        Map<Long, CartItem> items = cartItems.get(cartId);
        if (items == null) {
            throw new IllegalArgumentException("Cart not found: " + cartId);
        }
        
        // Check if the product already exists in the cart
        Optional<CartItem> existingItem = items.values().stream()
                .filter(i -> i.getProductId().equals(item.getProductId()))
                .findFirst();
                
        if (existingItem.isPresent()) {
            // Update existing item quantity
            CartItem updatedItem = existingItem.get();
            updatedItem.setQuantity(updatedItem.getQuantity() + item.getQuantity());
            items.put(updatedItem.getItemId(), updatedItem);
            updateCartItems(cartId);
            return updatedItem;
        } else {
            // Add new item
            if (item.getItemId() == null) {
                item.setItemId(nextItemId++);
            }
            items.put(item.getItemId(), item);
            updateCartItems(cartId);
            return item;
        }
    }

    /**
     * Updates an item in a shopping cart.
     *
     * @param cartId The cart ID
     * @param itemId The item ID
     * @param item The updated item
     * @return The updated cart item
     */
    public CartItem updateItem(Long cartId, Long itemId, CartItem item) {
        Map<Long, CartItem> items = cartItems.get(cartId);
        if (items == null || !items.containsKey(itemId)) {
            throw new IllegalArgumentException("Item not found in cart");
        }
        
        item.setItemId(itemId);
        items.put(itemId, item);
        updateCartItems(cartId);
        
        return item;
    }

    /**
     * Removes an item from a shopping cart.
     *
     * @param cartId The cart ID
     * @param itemId The item ID
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(Long cartId, Long itemId) {
        Map<Long, CartItem> items = cartItems.get(cartId);
        if (items == null) {
            return false;
        }
        
        boolean removed = items.remove(itemId) != null;
        if (removed) {
            updateCartItems(cartId);
        }
        
        return removed;
    }

    /**
     * Clears all items from a shopping cart.
     *
     * @param cartId The cart ID
     * @return true if the cart was cleared, false if the cart wasn't found
     */
    public boolean clearCart(Long cartId) {
        Map<Long, CartItem> items = cartItems.get(cartId);
        if (items == null) {
            return false;
        }
        
        items.clear();
        updateCartItems(cartId);
        
        return true;
    }

    /**
     * Deletes a shopping cart.
     *
     * @param cartId The cart ID
     * @return true if the cart was deleted, false if not found
     */
    public boolean deleteCart(Long cartId) {
        cartItems.remove(cartId);
        return carts.remove(cartId) != null;
    }

    /**
     * Gets all shopping carts.
     *
     * @return A list of all shopping carts
     */
    public List<ShoppingCart> findAll() {
        return new ArrayList<>(carts.values());
    }
    
    /**
     * Updates the items list in a shopping cart and updates the timestamp.
     *
     * @param cartId The cart ID
     */
    private void updateCartItems(Long cartId) {
        ShoppingCart cart = carts.get(cartId);
        if (cart != null) {
            cart.setItems(new ArrayList<>(cartItems.get(cartId).values()));
            cart.setUpdatedAt(LocalDateTime.now());
        }
    }
}
