package io.microprofile.tutorial.graphql.product.entity;

import org.eclipse.microprofile.graphql.Type;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Enum;
import org.eclipse.microprofile.graphql.NumberFormat;
import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.Ignore;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product entity representing a product in the catalog
 */
@Type("Product")
@Description("A product in the catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product implements Identifiable {
    
    @Description("Unique product identifier")
    private Long id;
    
    @NonNull
    @Description("Product name")
    private String name;
    
    @Description("Product description")
    private String description;
    
    @NonNull
    @Description("Product price in USD")
    @NumberFormat(value = "$ #,##0.00", locale = "en-US")
    private Double price;
    
    @Description("Product category")
    private String category;
    
    @Description("Stock quantity available")
    private Integer stockQuantity;
    
    @Description("Product release date")
    @DateFormat(value = "dd MMM yyyy")
    private LocalDate releaseDate;
    
    @Description("Current stock status")
    private StockStatus stockStatus;
    
    @Ignore
    @Description("Internal code for inventory management - excluded from GraphQL schema")
    private String internalCode;
    
    @Description("Audit log for product changes - excluded from output type only")
    private String auditLog;
    
    @Description("Tax rate for price calculations")
    private Double taxRate;
    
    /**
     * Stock status enumeration
     */
    @Enum("StockStatus")
    public enum StockStatus {
        IN_STOCK, 
        LOW_STOCK, 
        OUT_OF_STOCK
    }
    
    // Computed field - price with tax
    public Double getPriceWithTax() {
        if (price == null) return null;
        double rate = (taxRate != null) ? taxRate : 0.08; // Default 8% tax
        return price * (1 + rate);
    }
    
    // Computed field with logic - display name in uppercase
    public String getDisplayName() {
        return name != null ? name.toUpperCase() : "UNKNOWN";
    }
    
    // Computed field - availability status based on stock quantity
    public StockStatus getAvailabilityStatus() {
        if (stockQuantity == null || stockQuantity == 0) {
            return StockStatus.OUT_OF_STOCK;
        } else if (stockQuantity < 10) {
            return StockStatus.LOW_STOCK;
        } else {
            return StockStatus.IN_STOCK;
        }
    }
    
    // Audit log getter with @Ignore to exclude from output type only
    @Ignore
    public String getAuditLog() {
        return auditLog;
    }
}