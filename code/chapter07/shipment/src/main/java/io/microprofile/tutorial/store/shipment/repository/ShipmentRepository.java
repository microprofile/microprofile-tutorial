package io.microprofile.tutorial.store.shipment.repository;

import io.microprofile.tutorial.store.shipment.entity.Shipment;
import io.microprofile.tutorial.store.shipment.entity.ShipmentStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple in-memory repository for Shipment objects.
 * This class provides CRUD operations for Shipment entities.
 */
@ApplicationScoped
public class ShipmentRepository {

    private final Map<Long, Shipment> shipments = new ConcurrentHashMap<>();
    private long nextId = 1;

    /**
     * Saves a shipment to the repository.
     * If the shipment has no ID, a new ID is assigned.
     *
     * @param shipment The shipment to save
     * @return The saved shipment with ID assigned
     */
    public Shipment save(Shipment shipment) {
        if (shipment.getShipmentId() == null) {
            shipment.setShipmentId(nextId++);
        }
        
        if (shipment.getCreatedAt() == null) {
            shipment.setCreatedAt(LocalDateTime.now());
        }
        
        shipment.setUpdatedAt(LocalDateTime.now());
        
        shipments.put(shipment.getShipmentId(), shipment);
        return shipment;
    }

    /**
     * Finds a shipment by ID.
     *
     * @param id The shipment ID
     * @return An Optional containing the shipment if found, or empty if not found
     */
    public Optional<Shipment> findById(Long id) {
        return Optional.ofNullable(shipments.get(id));
    }

    /**
     * Finds shipments by order ID.
     *
     * @param orderId The order ID
     * @return A list of shipments for the specified order
     */
    public List<Shipment> findByOrderId(Long orderId) {
        return shipments.values().stream()
                .filter(shipment -> shipment.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments by tracking number.
     *
     * @param trackingNumber The tracking number
     * @return A list of shipments with the specified tracking number
     */
    public List<Shipment> findByTrackingNumber(String trackingNumber) {
        return shipments.values().stream()
                .filter(shipment -> trackingNumber.equals(shipment.getTrackingNumber()))
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments by status.
     *
     * @param status The shipment status
     * @return A list of shipments with the specified status
     */
    public List<Shipment> findByStatus(ShipmentStatus status) {
        return shipments.values().stream()
                .filter(shipment -> shipment.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Finds shipments that are expected to be delivered by a certain date.
     *
     * @param deliveryDate The delivery date
     * @return A list of shipments expected to be delivered by the specified date
     */
    public List<Shipment> findByEstimatedDeliveryBefore(LocalDateTime deliveryDate) {
        return shipments.values().stream()
                .filter(shipment -> shipment.getEstimatedDelivery() != null && 
                                   shipment.getEstimatedDelivery().isBefore(deliveryDate))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all shipments from the repository.
     *
     * @return A list of all shipments
     */
    public List<Shipment> findAll() {
        return new ArrayList<>(shipments.values());
    }

    /**
     * Deletes a shipment by ID.
     *
     * @param id The ID of the shipment to delete
     * @return true if the shipment was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        return shipments.remove(id) != null;
    }

    /**
     * Updates an existing shipment.
     *
     * @param id The ID of the shipment to update
     * @param shipment The updated shipment information
     * @return An Optional containing the updated shipment, or empty if not found
     */
    public Optional<Shipment> update(Long id, Shipment shipment) {
        if (!shipments.containsKey(id)) {
            return Optional.empty();
        }
        
        // Preserve creation date
        LocalDateTime createdAt = shipments.get(id).getCreatedAt();
        shipment.setCreatedAt(createdAt);
        
        shipment.setShipmentId(id);
        shipment.setUpdatedAt(LocalDateTime.now());
        
        shipments.put(id, shipment);
        return Optional.of(shipment);
    }
}
