package io.microprofile.tutorial.store.payment.client;

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
public class OrderServiceClient {

    private static final Logger LOGGER = Logger.getLogger(OrderServiceClient.class.getName());

    @ConfigProperty(name = "order.service.url", defaultValue = "http://localhost:8050/order")
    private String orderServiceUrl;

    /**
     * Updates the order status after a payment has been processed.
     *
     * @param orderId The ID of the order to update
     * @param newStatus The new status for the order
     * @return true if the update was successful, false otherwise
     */
    @Retry(maxRetries = 3, delay = 1000, jitter = 200)
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
     * Fallback method for updateOrderStatus.
     * Logs the failure and returns false.
     *
     * @param orderId The ID of the order
     * @param newStatus The new status for the order
     * @return false, indicating failure
     */
    public boolean updateOrderStatusFallback(Long orderId, String newStatus) {
        LOGGER.warning(String.format("Using fallback for order status update. Order ID: %d, Status: %s", orderId, newStatus));
        // In a production environment, you might store the failed update attempt in a database or message queue
        // for later processing
        return false;
    }
}
