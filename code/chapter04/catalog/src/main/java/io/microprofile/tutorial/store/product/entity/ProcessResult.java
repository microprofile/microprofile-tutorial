package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Result object for async product processing.
 */
@Schema(description = "Result of async product processing")
public class ProcessResult {
    
    @Schema(description = "ID of the processed product", example = "123")
    private Long productId;
    
    @Schema(description = "Processing status", 
            example = "COMPLETED", 
            enumeration = {"COMPLETED", "FAILED", "PENDING"})
    private String status;
    
    @Schema(description = "Processing message or error details", 
            example = "Product processed successfully")
    private String message;
    
    @Schema(description = "Timestamp when processing completed", 
            example = "2025-01-31T16:00:00Z")
    private String timestamp;
    
    public ProcessResult() {
    }
    
    public ProcessResult(Long productId, String status, String message) {
        this.productId = productId;
        this.status = status;
        this.message = message;
        this.timestamp = java.time.Instant.now().toString();
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
