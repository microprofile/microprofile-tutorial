package io.microprofile.tutorial.store.shoppingcart.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for communicating with the Catalog Service.
 */
@ApplicationScoped
public class CatalogClient {

    private static final Logger LOGGER = Logger.getLogger(CatalogClient.class.getName());

    @ConfigProperty(name = "catalog.service.url", defaultValue = "http://localhost:5050/catalog")
    private String catalogServiceUrl;
    
    // Cache for product details to reduce service calls
    private final Map<Long, ProductInfo> productCache = new HashMap<>();

    /**
     * Gets product information from the catalog service.
     *
     * @param productId The product ID
     * @return ProductInfo containing product details
     */
    // @Retry(maxRetries = 3, delay = 1000, jitter = 200, unit = ChronoUnit.MILLIS)
    // @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    // @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)
    // @Fallback(fallbackMethod = "getProductInfoFallback")
    public ProductInfo getProductInfo(Long productId) {
        // Check cache first
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }
        
        LOGGER.info(String.format("Fetching product info for product %d", productId));
        
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            String url = String.format("%s/api/products/%d", catalogServiceUrl, productId);
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
                    
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                // Simple parsing - in a real app, use proper JSON parsing
                String name = extractField(jsonResponse, "name");
                String priceStr = extractField(jsonResponse, "price");
                
                double price = 0.0;
                try {
                    price = Double.parseDouble(priceStr);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Failed to parse product price: " + priceStr);
                }
                
                ProductInfo productInfo = new ProductInfo(productId, name, price);
                
                // Cache the result
                productCache.put(productId, productInfo);
                
                return productInfo;
            }
            
            LOGGER.warning(String.format("Failed to get product info. Status code: %d", response.getStatus()));
            return new ProductInfo(productId, "Unknown Product", 0.0);
        } catch (ProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to Catalog Service", e);
            throw e;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Fallback method for getProductInfo.
     * Returns a placeholder product info when the catalog service is unavailable.
     *
     * @param productId The product ID
     * @return A placeholder ProductInfo object
     */
    public ProductInfo getProductInfoFallback(Long productId) {
        LOGGER.warning(String.format("Using fallback for product info. Product ID: %d", productId));
        
        // Check if we have a cached version
        if (productCache.containsKey(productId)) {
            return productCache.get(productId);
        }
        
        // Return a placeholder
        return new ProductInfo(
            productId,
            "Product " + productId + " (Service Unavailable)",
            0.0
        );
    }
    
    /**
     * Helper method to extract field values from JSON string.
     * This is a simplified approach - in a real app, use a proper JSON parser.
     *
     * @param jsonString The JSON string
     * @param fieldName The name of the field to extract
     * @return The extracted field value
     */
    private String extractField(String jsonString, String fieldName) {
        String searchPattern = "\"" + fieldName + "\":";
        if (jsonString.contains(searchPattern)) {
            int startIndex = jsonString.indexOf(searchPattern) + searchPattern.length();
            int endIndex;
            
            // Skip whitespace
            while (startIndex < jsonString.length() && 
                   (jsonString.charAt(startIndex) == ' ' || jsonString.charAt(startIndex) == '\t')) {
                startIndex++;
            }
            
            if (startIndex < jsonString.length() && jsonString.charAt(startIndex) == '"') {
                // String value
                startIndex++; // Skip opening quote
                endIndex = jsonString.indexOf("\"", startIndex);
            } else {
                // Number or boolean value
                endIndex = jsonString.indexOf(",", startIndex);
                if (endIndex == -1) {
                    endIndex = jsonString.indexOf("}", startIndex);
                }
            }
            
            if (endIndex > startIndex) {
                return jsonString.substring(startIndex, endIndex);
            }
        }
        return "";
    }
    
    /**
     * Inner class to hold product information.
     */
    public static class ProductInfo {
        private final Long productId;
        private final String name;
        private final double price;
        
        public ProductInfo(Long productId, String name, double price) {
            this.productId = productId;
            this.name = name;
            this.price = price;
        }
        
        public Long getProductId() {
            return productId;
        }
        
        public String getName() {
            return name;
        }
        
        public double getPrice() {
            return price;
        }
    }
}
