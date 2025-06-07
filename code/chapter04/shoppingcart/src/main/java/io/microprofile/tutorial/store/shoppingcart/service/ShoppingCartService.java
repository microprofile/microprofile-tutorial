package io.microprofile.tutorial.store.shoppingcart.service;

import io.microprofile.tutorial.store.shoppingcart.client.CatalogClient;
import io.microprofile.tutorial.store.shoppingcart.client.InventoryClient;
import io.microprofile.tutorial.store.shoppingcart.entity.CartItem;
import io.microprofile.tutorial.store.shoppingcart.entity.ShoppingCart;
import io.microprofile.tutorial.store.shoppingcart.repository.ShoppingCartRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Service class for Shopping Cart management operations.
 */
@ApplicationScoped
public class ShoppingCartService {

    private static final Logger LOGGER = Logger.getLogger(ShoppingCartService.class.getName());

    @Inject
    private ShoppingCartRepository cartRepository;
    
    @Inject
    private InventoryClient inventoryClient;
    
    @Inject
    private CatalogClient catalogClient;

    /**
     * Gets a shopping cart for a user, creating one if it doesn't exist.
     *
     * @param userId The user ID
     * @return The user's shopping cart
     */
    public ShoppingCart getOrCreateCart(Long userId) {
        Optional<ShoppingCart> existingCart = cartRepository.findByUserId(userId);
        
        return existingCart.orElseGet(() -> {
            LOGGER.info("Creating new cart for user: " + userId);
            return cartRepository.createCart(userId);
        });
    }

    /**
     * Gets a shopping cart by ID.
     *
     * @param cartId The cart ID
     * @return The shopping cart
     * @throws WebApplicationException if the cart is not found
     */
    public ShoppingCart getCartById(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new WebApplicationException("Cart not found", Response.Status.NOT_FOUND));
    }

    /**
     * Gets a user's shopping cart.
     *
     * @param userId The user ID
     * @return The user's shopping cart
     * @throws WebApplicationException if the cart is not found
     */
    public ShoppingCart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new WebApplicationException("Cart not found for user", Response.Status.NOT_FOUND));
    }

    /**
     * Gets all shopping carts.
     *
     * @return A list of all shopping carts
     */
    public List<ShoppingCart> getAllCarts() {
        return cartRepository.findAll();
    }

    /**
     * Adds an item to a shopping cart.
     *
     * @param cartId The cart ID
     * @param item The item to add
     * @return The updated cart item
     * @throws WebApplicationException if the cart is not found or inventory is insufficient
     */
    public CartItem addItemToCart(Long cartId, CartItem item) {
        // Verify the cart exists
        getCartById(cartId);
        
        // Check inventory availability
        boolean isAvailable = inventoryClient.checkProductAvailability(item.getProductId(), item.getQuantity());
        if (!isAvailable) {
            throw new WebApplicationException("Insufficient inventory for product: " + item.getProductId(), 
                                             Response.Status.BAD_REQUEST);
        }
        
        // Enrich item with product details if needed
        if (item.getProductName() == null || item.getPrice() == 0) {
            CatalogClient.ProductInfo productInfo = catalogClient.getProductInfo(item.getProductId());
            item.setProductName(productInfo.getName());
            item.setPrice(productInfo.getPrice());
        }
        
        LOGGER.info(String.format("Adding item to cart %d: %s, quantity %d", 
                                 cartId, item.getProductName(), item.getQuantity()));
        
        return cartRepository.addItem(cartId, item);
    }

    /**
     * Updates an item in a shopping cart.
     *
     * @param cartId The cart ID
     * @param itemId The item ID
     * @param item The updated item
     * @return The updated cart item
     * @throws WebApplicationException if the cart or item is not found or inventory is insufficient
     */
    public CartItem updateCartItem(Long cartId, Long itemId, CartItem item) {
        // Verify the cart exists
        ShoppingCart cart = getCartById(cartId);
        
        // Verify the item exists
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getItemId().equals(itemId))
                .findFirst();
        
        if (!existingItem.isPresent()) {
            throw new WebApplicationException("Item not found in cart", Response.Status.NOT_FOUND);
        }
        
        // Check inventory availability if quantity is increasing
        CartItem currentItem = existingItem.get();
        if (item.getQuantity() > currentItem.getQuantity()) {
            int additionalQuantity = item.getQuantity() - currentItem.getQuantity();
            boolean isAvailable = inventoryClient.checkProductAvailability(
                currentItem.getProductId(), additionalQuantity);
                
            if (!isAvailable) {
                throw new WebApplicationException("Insufficient inventory for product: " + currentItem.getProductId(), 
                                                Response.Status.BAD_REQUEST);
            }
        }
        
        // Preserve product information
        item.setProductId(currentItem.getProductId());
        
        // If no product name is provided, use the existing one
        if (item.getProductName() == null) {
            item.setProductName(currentItem.getProductName());
        }
        
        // If no price is provided, use the existing one
        if (item.getPrice() == 0) {
            item.setPrice(currentItem.getPrice());
        }
        
        LOGGER.info(String.format("Updating item %d in cart %d: new quantity %d", 
                                 itemId, cartId, item.getQuantity()));
        
        return cartRepository.updateItem(cartId, itemId, item);
    }

    /**
     * Removes an item from a shopping cart.
     *
     * @param cartId The cart ID
     * @param itemId The item ID
     * @throws WebApplicationException if the cart or item is not found
     */
    public void removeItemFromCart(Long cartId, Long itemId) {
        // Verify the cart exists
        getCartById(cartId);
        
        boolean removed = cartRepository.removeItem(cartId, itemId);
        if (!removed) {
            throw new WebApplicationException("Item not found in cart", Response.Status.NOT_FOUND);
        }
        
        LOGGER.info(String.format("Removed item %d from cart %d", itemId, cartId));
    }

    /**
     * Clears all items from a shopping cart.
     *
     * @param cartId The cart ID
     * @throws WebApplicationException if the cart is not found
     */
    public void clearCart(Long cartId) {
        // Verify the cart exists
        getCartById(cartId);
        
        boolean cleared = cartRepository.clearCart(cartId);
        if (!cleared) {
            throw new WebApplicationException("Failed to clear cart", Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        LOGGER.info(String.format("Cleared cart %d", cartId));
    }

    /**
     * Deletes a shopping cart.
     *
     * @param cartId The cart ID
     * @throws WebApplicationException if the cart is not found
     */
    public void deleteCart(Long cartId) {
        // Verify the cart exists
        getCartById(cartId);
        
        boolean deleted = cartRepository.deleteCart(cartId);
        if (!deleted) {
            throw new WebApplicationException("Failed to delete cart", Response.Status.INTERNAL_SERVER_ERROR);
        }
        
        LOGGER.info(String.format("Deleted cart %d", cartId));
    }
}
