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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for communicating with the Inventory Service.
 */
@ApplicationScoped
public class InventoryClient {

    private static final Logger LOGGER = Logger.getLogger(InventoryClient.class.getName());

    @ConfigProperty(name = "inventory.service.url", defaultValue = "http://localhost:7050/inventory")
    private String inventoryServiceUrl;

    /**
     * Checks if a product is available in sufficient quantity.
     *
     * @param productId The product ID
     * @param quantity The requested quantity
     * @return true if the product is available in the requested quantity, false otherwise
     */
    // @Retry(maxRetries = 3, delay = 1000, jitter = 200, unit = ChronoUnit.MILLIS)
    // @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    // @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)
    // @Fallback(fallbackMethod = "checkProductAvailabilityFallback")
    public boolean checkProductAvailability(Long productId, int quantity) {
        LOGGER.info(String.format("Checking availability for product %d, quantity %d", productId, quantity));
        
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            String url = String.format("%s/api/inventories/product/%d", inventoryServiceUrl, productId);
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
                    
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                // Simple parsing - in a real app, use proper JSON parsing
                if (jsonResponse.contains("\"quantity\":")) {
                    String quantityStr = jsonResponse.split("\"quantity\":")[1].split(",")[0].trim();
                    int availableQuantity = Integer.parseInt(quantityStr);
                    return availableQuantity >= quantity;
                }
            }
            
            LOGGER.warning(String.format("Failed to check product availability. Status code: %d", response.getStatus()));
            return false;
        } catch (ProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to Inventory Service", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error parsing inventory response", e);
            return false;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Fallback method for checkProductAvailability.
     * Always returns true to allow the cart operation to continue,
     * but logs a warning.
     *
     * @param productId The product ID
     * @param quantity The requested quantity
     * @return true, allowing the operation to proceed
     */
    public boolean checkProductAvailabilityFallback(Long productId, int quantity) {
        LOGGER.warning(String.format(
            "Using fallback for product availability check. Product ID: %d, Quantity: %d", 
            productId, quantity));
        // In a production system, you might want to cache product availability
        // or implement a more sophisticated fallback mechanism
        return true; // Allow the operation to proceed
    }
}
