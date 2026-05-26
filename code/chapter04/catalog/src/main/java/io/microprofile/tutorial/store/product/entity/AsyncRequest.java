package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Request object for async product processing.
 */
@Schema(description = "Async product processing request")
public class AsyncRequest {
    
    @Schema(description = "Product to process", required = true)
    private Product product;
    
    @Schema(description = "Callback URL to notify when processing completes", 
            example = "https://example.com/callback", 
            required = true)
    private String callbackUrl;
    
    public AsyncRequest() {
    }
    
    public AsyncRequest(Product product, String callbackUrl) {
        this.product = product;
        this.callbackUrl = callbackUrl;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public String getCallbackUrl() {
        return callbackUrl;
    }
    
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
