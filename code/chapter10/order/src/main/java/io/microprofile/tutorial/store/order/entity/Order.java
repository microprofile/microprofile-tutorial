package io.microprofile.tutorial.store.order.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.json.bind.annotation.JsonbPropertyOrder;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order entity representing an order in the e-commerce system.
 * This class uses Lombok annotations for boilerplate code generation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonbPropertyOrder({"id", "customerId", "customerEmail", "status", "totalAmount", "currency", "items", "shippingAddress", "billingAddress", "orderDate", "lastModified"})
public class Order {
    
    /**
     * Unique identifier for the order
     */
    private Long id;
    
    /**
     * Customer identifier who placed the order
     */
    @NotBlank(message = "Customer ID cannot be blank")
    private String customerId;
    
    /**
     * Customer email address
     */
    @NotBlank(message = "Customer email cannot be blank")
    private String customerEmail;
    
    /**
     * Current status of the order
     */
    @NotNull(message = "Order status cannot be null")
    private OrderStatus status;
    
    /**
     * Total amount for the order
     */
    @NotNull(message = "Total amount cannot be null")
    @PositiveOrZero(message = "Total amount must be zero or positive")
    private BigDecimal totalAmount;
    
    /**
     * Currency code (e.g., USD, EUR)
     */
    @NotBlank(message = "Currency cannot be blank")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    private String currency;
    
    /**
     * List of items in the order
     */
    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItem> items;
    
    /**
     * Shipping address for the order
     */
    @NotNull(message = "Shipping address cannot be null")
    private Address shippingAddress;
    
    /**
     * Billing address for the order (can be same as shipping)
     */
    @NotNull(message = "Billing address cannot be null")
    private Address billingAddress;
    
    /**
     * Date and time when the order was created
     */
    private LocalDateTime orderDate;
    
    /**
     * Date and time when the order was last modified
     */
    private LocalDateTime lastModified;
    
    /**
     * Order status enumeration
     */
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        REFUNDED
    }
    
    /**
     * Order item representing a product in the order
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonbPropertyOrder({"productId", "productName", "quantity", "unitPrice", "totalPrice"})
    public static class OrderItem {
        
        /**
         * Product identifier
         */
        @NotBlank(message = "Product ID cannot be blank")
        private String productId;
        
        /**
         * Product name
         */
        @NotBlank(message = "Product name cannot be blank")
        private String productName;
        
        /**
         * Quantity ordered
         */
        @NotNull(message = "Quantity cannot be null")
        @Positive(message = "Quantity must be positive")
        private Integer quantity;
        
        /**
         * Unit price of the product
         */
        @NotNull(message = "Unit price cannot be null")
        @PositiveOrZero(message = "Unit price must be zero or positive")
        private BigDecimal unitPrice;
        
        /**
         * Total price for this item (quantity * unitPrice)
         */
        @NotNull(message = "Total price cannot be null")
        @PositiveOrZero(message = "Total price must be zero or positive")
        private BigDecimal totalPrice;
    }
    
    /**
     * Address information for shipping/billing
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonbPropertyOrder({"street", "city", "state", "postalCode", "country"})
    public static class Address {
        
        /**
         * Street address
         */
        @NotBlank(message = "Street address cannot be blank")
        private String street;
        
        /**
         * City name
         */
        @NotBlank(message = "City cannot be blank")
        private String city;
        
        /**
         * State or province
         */
        @NotBlank(message = "State cannot be blank")
        private String state;
        
        /**
         * Postal/ZIP code
         */
        @NotBlank(message = "Postal code cannot be blank")
        private String postalCode;
        
        /**
         * Country name or code
         */
        @NotBlank(message = "Country cannot be blank")
        private String country;
    }
}
