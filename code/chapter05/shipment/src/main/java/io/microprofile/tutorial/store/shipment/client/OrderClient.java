package io.microprofile.tutorial.store.shipment.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
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
 * Client for communicating with the Order Service.
 */
@ApplicationScoped
public class OrderClient {

    private static final Logger LOGGER = Logger.getLogger(OrderClient.class.getName());

    @ConfigProperty(name = "order.service.url", defaultValue = "http://localhost:8050/order")
    private String orderServiceUrl;

    /**
     * Updates the order status after a shipment has been processed.
     *
     * @param orderId The ID of the order to update
     * @param newStatus The new status for the order
     * @return true if the update was successful, false otherwise
     */
    @Retry(maxRetries = 3, delay = 1000, jitter = 200, unit = ChronoUnit.MILLIS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)
    @Fallback(fallbackMethod = "updateOrderStatusFallback")
    public boolean updateOrderStatus(Long orderId, String newStatus) {
        LOGGER.info(String.format("Updating order %d status to %s", orderId, newStatus));
        
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            String url = String.format("%s/api/orders/%d/status/%s", orderServiceUrl, orderId, newStatus);
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json("{}"));
            
            boolean success = response.getStatus() == Response.Status.OK.getStatusCode();
            if (!success) {
                LOGGER.warning(String.format("Failed to update order status. Status code: %d", response.getStatus()));
            }
            return success;
        } catch (ProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to Order Service", e);
            throw e;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Verifies that an order exists and is in a valid state for shipment.
     *
     * @param orderId The ID of the order to verify
     * @return true if the order exists and is in a valid state, false otherwise
     */
    @Retry(maxRetries = 3, delay = 1000, jitter = 200, unit = ChronoUnit.MILLIS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)
    @Fallback(fallbackMethod = "verifyOrderFallback")
    public boolean verifyOrder(Long orderId) {
        LOGGER.info(String.format("Verifying order %d for shipment", orderId));
        
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            String url = String.format("%s/api/orders/%d", orderServiceUrl, orderId);
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                // Simple check if the order is in a valid state for shipment
                // In a real app, we'd parse the JSON properly
                return jsonResponse.contains("\"status\":\"PAID\"") || 
                       jsonResponse.contains("\"status\":\"PROCESSING\"") ||
                       jsonResponse.contains("\"status\":\"READY_FOR_SHIPMENT\"");
            }
            
            LOGGER.warning(String.format("Failed to verify order. Status code: %d", response.getStatus()));
            return false;
        } catch (ProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to Order Service", e);
            throw e;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Gets the shipping address for an order.
     *
     * @param orderId The ID of the order
     * @return The shipping address, or null if not found
     */
    @Retry(maxRetries = 3, delay = 1000, jitter = 200, unit = ChronoUnit.MILLIS)
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000, successThreshold = 2)
    @Fallback(fallbackMethod = "getShippingAddressFallback")
    public String getShippingAddress(Long orderId) {
        LOGGER.info(String.format("Getting shipping address for order %d", orderId));
        
        Client client = null;
        try {
            client = ClientBuilder.newClient();
            String url = String.format("%s/api/orders/%d", orderServiceUrl, orderId);
            
            Response response = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                String jsonResponse = response.readEntity(String.class);
                // Simple extract of shipping address - in real app use proper JSON parsing
                if (jsonResponse.contains("\"shippingAddress\":")) {
                    int startIndex = jsonResponse.indexOf("\"shippingAddress\":") + "\"shippingAddress\":".length();
                    startIndex = jsonResponse.indexOf("\"", startIndex) + 1;
                    int endIndex = jsonResponse.indexOf("\"", startIndex);
                    if (endIndex > startIndex) {
                        return jsonResponse.substring(startIndex, endIndex);
                    }
                }
            }
            
            LOGGER.warning(String.format("Failed to get shipping address. Status code: %d", response.getStatus()));
            return null;
        } catch (ProcessingException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to Order Service", e);
            throw e;
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    /**
     * Fallback method for updateOrderStatus.
     *
     * @param orderId The ID of the order
     * @param newStatus The new status for the order
     * @return false, indicating failure
     */
    public boolean updateOrderStatusFallback(Long orderId, String newStatus) {
        LOGGER.warning(String.format("Using fallback for order status update. Order ID: %d, Status: %s", orderId, newStatus));
        return false;
    }

    /**
     * Fallback method for verifyOrder.
     *
     * @param orderId The ID of the order
     * @return false, indicating failure
     */
    public boolean verifyOrderFallback(Long orderId) {
        LOGGER.warning(String.format("Using fallback for order verification. Order ID: %d", orderId));
        return false;
    }

    /**
     * Fallback method for getShippingAddress.
     *
     * @param orderId The ID of the order
     * @return null, indicating failure
     */
    public String getShippingAddressFallback(Long orderId) {
        LOGGER.warning(String.format("Using fallback for getting shipping address. Order ID: %d", orderId));
        return null;
    }
}
