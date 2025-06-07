package io.microprofile.tutorial.store.payment.repository;

import io.microprofile.tutorial.store.payment.entity.Payment;
import io.microprofile.tutorial.store.payment.entity.PaymentStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Simple in-memory repository for Payment objects.
 * This class provides CRUD operations for Payment entities to demonstrate MicroProfile concepts.
 */
@ApplicationScoped
public class PaymentRepository {

    private final Map<Long, Payment> payments = new HashMap<>();
    private long nextId = 1;

    /**
     * Saves a payment to the repository.
     * If the payment has no ID, a new ID is assigned.
     *
     * @param payment The payment to save
     * @return The saved payment with ID assigned
     */
    public Payment save(Payment payment) {
        if (payment.getPaymentId() == null) {
            payment.setPaymentId(nextId++);
        }
        payments.put(payment.getPaymentId(), payment);
        return payment;
    }

    /**
     * Finds a payment by ID.
     *
     * @param id The payment ID
     * @return An Optional containing the payment if found, or empty if not found
     */
    public Optional<Payment> findById(Long id) {
        return Optional.ofNullable(payments.get(id));
    }

    /**
     * Finds payments by user ID.
     *
     * @param userId The user ID
     * @return A list of payments for the specified user
     */
    public List<Payment> findByUserId(Long userId) {
        return payments.values().stream()
                .filter(payment -> payment.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Finds payments by order ID.
     *
     * @param orderId The order ID
     * @return A list of payments for the specified order
     */
    public List<Payment> findByOrderId(Long orderId) {
        return payments.values().stream()
                .filter(payment -> payment.getOrderId().equals(orderId))
                .collect(Collectors.toList());
    }

    /**
     * Finds payments by status.
     *
     * @param status The payment status
     * @return A list of payments with the specified status
     */
    public List<Payment> findByStatus(PaymentStatus status) {
        return payments.values().stream()
                .filter(payment -> payment.getStatus().equals(status))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all payments from the repository.
     *
     * @return A list of all payments
     */
    public List<Payment> findAll() {
        return new ArrayList<>(payments.values());
    }

    /**
     * Deletes a payment by ID.
     *
     * @param id The ID of the payment to delete
     * @return true if the payment was deleted, false if not found
     */
    public boolean deleteById(Long id) {
        return payments.remove(id) != null;
    }

    /**
     * Updates an existing payment.
     *
     * @param id The ID of the payment to update
     * @param payment The updated payment information
     * @return An Optional containing the updated payment, or empty if not found
     */
    public Optional<Payment> update(Long id, Payment payment) {
        if (!payments.containsKey(id)) {
            return Optional.empty();
        }
        
        payment.setPaymentId(id);
        payments.put(id, payment);
        return Optional.of(payment);
    }
}
