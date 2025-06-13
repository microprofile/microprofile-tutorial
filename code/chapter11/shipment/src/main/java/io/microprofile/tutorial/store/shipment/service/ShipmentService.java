package io.microprofile.tutorial.store.shipment.service;

import io.microprofile.tutorial.store.shipment.client.OrderClient;
import io.microprofile.tutorial.store.shipment.entity.Shipment;
import io.microprofile.tutorial.store.shipment.entity.ShipmentStatus;
import io.microprofile.tutorial.store.shipment.repository.ShipmentRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

/**
 * Shipment Service for managing shipments.
 */
@ApplicationScoped
public class ShipmentService {

    private static final Logger LOGGER = Logger.getLogger(ShipmentService.class.getName());
    private static final Random RANDOM = new Random();
    private static final String[] CARRIERS = {"FedEx", "UPS", "USPS", "DHL", "Amazon Logistics"};

    @Inject
    private ShipmentRepository shipmentRepository;

    @Inject
    private OrderClient orderClient;

    /**
     * Creates a new shipment for an order.
     *
     * @param orderId The order ID
     * @return The created shipment, or null if the order is invalid
     */
    @Counted(name = "shipmentCreations", description = "Number of shipments created")
    @Timed(name = "createShipmentTimer", description = "Time to create a shipment")
    public Shipment createShipment(Long orderId) {
        LOGGER.info("Creating shipment for order: " + orderId);

        // Verify that the order exists and is ready for shipment
        if (!orderClient.verifyOrder(orderId)) {
            LOGGER.warning("Order " + orderId + " is not valid for shipment");
            return null;
        }

        // Get shipping address from order service
        String shippingAddress = orderClient.getShippingAddress(orderId);
        if (shippingAddress == null) {
            LOGGER.warning("Could not retrieve shipping address for order " + orderId);
            return null;
        }

        // Create a new shipment
        Shipment shipment = Shipment.builder()
                .orderId(orderId)
                .status(ShipmentStatus.PENDING)
                .trackingNumber(generateTrackingNumber())
                .carrier(selectRandomCarrier())
                .shippingAddress(shippingAddress)
                .estimatedDelivery(LocalDateTime.now().plusDays(5))
                .createdAt(LocalDateTime.now())
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Update order status to indicate shipment is being processed
        orderClient.updateOrderStatus(orderId, "SHIPMENT_CREATED");
        
        return savedShipment;
    }

    /**
     * Updates the status of a shipment.
     *
     * @param shipmentId The shipment ID
     * @param status The new status
     * @return The updated shipment, or empty if not found
     */
    @Counted(name = "shipmentStatusUpdates", description = "Number of shipment status updates")
    public Optional<Shipment> updateShipmentStatus(Long shipmentId, ShipmentStatus status) {
        LOGGER.info("Updating shipment " + shipmentId + " status to " + status);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setStatus(status);
            shipment.setUpdatedAt(LocalDateTime.now());
            
            // If status is SHIPPED, set the shipped date
            if (status == ShipmentStatus.SHIPPED) {
                shipment.setShippedAt(LocalDateTime.now());
                orderClient.updateOrderStatus(shipment.getOrderId(), "SHIPPED");
            } 
            // If status is DELIVERED, update order status
            else if (status == ShipmentStatus.DELIVERED) {
                orderClient.updateOrderStatus(shipment.getOrderId(), "DELIVERED");
            }
            // If status is FAILED, update order status
            else if (status == ShipmentStatus.FAILED) {
                orderClient.updateOrderStatus(shipment.getOrderId(), "DELIVERY_FAILED");
            }
            
            return Optional.of(shipmentRepository.save(shipment));
        }
        
        return Optional.empty();
    }

    /**
     * Gets a shipment by ID.
     *
     * @param shipmentId The shipment ID
     * @return The shipment, or empty if not found
     */
    public Optional<Shipment> getShipment(Long shipmentId) {
        LOGGER.info("Getting shipment: " + shipmentId);
        return shipmentRepository.findById(shipmentId);
    }

    /**
     * Gets all shipments for an order.
     *
     * @param orderId The order ID
     * @return The list of shipments for the order
     */
    public List<Shipment> getShipmentsByOrder(Long orderId) {
        LOGGER.info("Getting shipments for order: " + orderId);
        return shipmentRepository.findByOrderId(orderId);
    }

    /**
     * Gets a shipment by tracking number.
     *
     * @param trackingNumber The tracking number
     * @return The shipment, or empty if not found
     */
    public Optional<Shipment> getShipmentByTrackingNumber(String trackingNumber) {
        LOGGER.info("Getting shipment with tracking number: " + trackingNumber);
        List<Shipment> shipments = shipmentRepository.findByTrackingNumber(trackingNumber);
        return shipments.isEmpty() ? Optional.empty() : Optional.of(shipments.get(0));
    }

    /**
     * Gets all shipments.
     *
     * @return All shipments
     */
    public List<Shipment> getAllShipments() {
        LOGGER.info("Getting all shipments");
        return shipmentRepository.findAll();
    }

    /**
     * Gets shipments by status.
     *
     * @param status The status
     * @return The list of shipments with the given status
     */
    public List<Shipment> getShipmentsByStatus(ShipmentStatus status) {
        LOGGER.info("Getting shipments with status: " + status);
        return shipmentRepository.findByStatus(status);
    }

    /**
     * Gets shipments due for delivery by the given date.
     *
     * @param date The date
     * @return The list of shipments due by the given date
     */
    public List<Shipment> getShipmentsDueBy(LocalDateTime date) {
        LOGGER.info("Getting shipments due by: " + date);
        return shipmentRepository.findByEstimatedDeliveryBefore(date);
    }

    /**
     * Updates the carrier for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param carrier The new carrier
     * @return The updated shipment, or empty if not found
     */
    public Optional<Shipment> updateCarrier(Long shipmentId, String carrier) {
        LOGGER.info("Updating carrier for shipment " + shipmentId + " to " + carrier);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setCarrier(carrier);
            shipment.setUpdatedAt(LocalDateTime.now());
            return Optional.of(shipmentRepository.save(shipment));
        }
        
        return Optional.empty();
    }

    /**
     * Updates the tracking number for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param trackingNumber The new tracking number
     * @return The updated shipment, or empty if not found
     */
    public Optional<Shipment> updateTrackingNumber(Long shipmentId, String trackingNumber) {
        LOGGER.info("Updating tracking number for shipment " + shipmentId + " to " + trackingNumber);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setTrackingNumber(trackingNumber);
            shipment.setUpdatedAt(LocalDateTime.now());
            return Optional.of(shipmentRepository.save(shipment));
        }
        
        return Optional.empty();
    }

    /**
     * Updates the estimated delivery date for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param estimatedDelivery The new estimated delivery date
     * @return The updated shipment, or empty if not found
     */
    public Optional<Shipment> updateEstimatedDelivery(Long shipmentId, LocalDateTime estimatedDelivery) {
        LOGGER.info("Updating estimated delivery for shipment " + shipmentId + " to " + estimatedDelivery);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setEstimatedDelivery(estimatedDelivery);
            shipment.setUpdatedAt(LocalDateTime.now());
            return Optional.of(shipmentRepository.save(shipment));
        }
        
        return Optional.empty();
    }

    /**
     * Updates the notes for a shipment.
     *
     * @param shipmentId The shipment ID
     * @param notes The new notes
     * @return The updated shipment, or empty if not found
     */
    public Optional<Shipment> updateNotes(Long shipmentId, String notes) {
        LOGGER.info("Updating notes for shipment " + shipmentId);
        
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            Shipment shipment = shipmentOpt.get();
            shipment.setNotes(notes);
            shipment.setUpdatedAt(LocalDateTime.now());
            return Optional.of(shipmentRepository.save(shipment));
        }
        
        return Optional.empty();
    }

    /**
     * Deletes a shipment.
     *
     * @param shipmentId The shipment ID
     * @return true if the shipment was deleted, false if not found
     */
    public boolean deleteShipment(Long shipmentId) {
        LOGGER.info("Deleting shipment: " + shipmentId);
        Optional<Shipment> shipmentOpt = shipmentRepository.findById(shipmentId);
        if (shipmentOpt.isPresent()) {
            // Only allow deletion if the shipment is in PENDING or PROCESSING status
            ShipmentStatus status = shipmentOpt.get().getStatus();
            if (status == ShipmentStatus.PENDING || status == ShipmentStatus.PROCESSING) {
                return shipmentRepository.deleteById(shipmentId);
            }
            LOGGER.warning("Cannot delete shipment with status: " + status);
            return false;
        }
        return false;
    }

    /**
     * Generates a random tracking number.
     *
     * @return A random tracking number
     */
    private String generateTrackingNumber() {
        return String.format("%s-%04d-%04d-%04d", 
                CARRIERS[RANDOM.nextInt(CARRIERS.length)].substring(0, 2).toUpperCase(),
                RANDOM.nextInt(10000), 
                RANDOM.nextInt(10000),
                RANDOM.nextInt(10000));
    }

    /**
     * Selects a random carrier.
     *
     * @return A random carrier
     */
    private String selectRandomCarrier() {
        return CARRIERS[RANDOM.nextInt(CARRIERS.length)];
    }
}
