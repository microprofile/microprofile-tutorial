package io.microprofile.tutorial.store.inventory.dto;

import io.microprofile.tutorial.store.inventory.entity.Inventory;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * DTO that combines inventory data with product information from the catalog service.
 */
public class InventoryWithProductInfo {
    
    @JsonbProperty("inventory")
    private Inventory inventory;
    
    @JsonbProperty("product")
    private Product product;
    
    /**
     * Default constructor for JSON-B.
     */
    public InventoryWithProductInfo() {
    }
    
    /**
     * Constructor with parameters.
     *
     * @param inventory The inventory information
     * @param product The product information from catalog service
     */
    public InventoryWithProductInfo(Inventory inventory, Product product) {
        this.inventory = inventory;
        this.product = product;
    }
    
    /**
     * Gets the inventory information.
     *
     * @return The inventory
     */
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * Sets the inventory information.
     *
     * @param inventory The inventory to set
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
    
    /**
     * Gets the product information.
     *
     * @return The product
     */
    public Product getProduct() {
        return product;
    }
    
    /**
     * Sets the product information.
     *
     * @param product The product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }
    
    /**
     * Gets the inventory ID.
     *
     * @return The inventory ID
     */
    @JsonbProperty("inventoryId")
    public Long getInventoryId() {
        return inventory != null ? inventory.getInventoryId() : null;
    }
    
    /**
     * Gets the product ID.
     *
     * @return The product ID
     */
    @JsonbProperty("productId")
    public Long getProductId() {
        return inventory != null ? inventory.getProductId() : null;
    }
    
    /**
     * Gets the product name.
     *
     * @return The product name
     */
    @JsonbProperty("productName")
    public String getProductName() {
        return product != null ? product.getName() : null;
    }
    
    /**
     * Gets the product price.
     *
     * @return The product price
     */
    @JsonbProperty("productPrice")
    public Double getProductPrice() {
        return product != null ? product.getPrice() : null;
    }
    
    /**
     * Gets the product category.
     *
     * @return The product category
     */
    @JsonbProperty("productCategory")
    public String getProductCategory() {
        return product != null ? product.getCategory() : null;
    }
    
    /**
     * Gets the inventory quantity.
     *
     * @return The quantity
     */
    @JsonbProperty("quantity")
    public Integer getQuantity() {
        return inventory != null ? inventory.getQuantity() : null;
    }
    
    /**
     * Gets the reserved quantity.
     *
     * @return The reserved quantity
     */
    @JsonbProperty("reservedQuantity")
    public Integer getReservedQuantity() {
        return inventory != null ? inventory.getReservedQuantity() : null;
    }
    
    /**
     * Gets the available quantity (quantity - reserved).
     *
     * @return The available quantity
     */
    @JsonbProperty("availableQuantity")
    public Integer getAvailableQuantity() {
        if (inventory == null) {
            return null;
        }
        return inventory.getQuantity() - inventory.getReservedQuantity();
    }
    
    @Override
    public String toString() {
        return "InventoryWithProductInfo{" +
                "inventoryId=" + getInventoryId() +
                ", productId=" + getProductId() +
                ", productName='" + getProductName() + '\'' +
                ", quantity=" + getQuantity() +
                ", availableQuantity=" + getAvailableQuantity() +
                ", price=" + getProductPrice() +
                ", category='" + getProductCategory() + '\'' +
                '}';
    }
}
