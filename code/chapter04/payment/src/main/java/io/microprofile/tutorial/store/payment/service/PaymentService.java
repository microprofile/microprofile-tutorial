package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.Payment;
import io.microprofile.tutorial.store.payment.entity.PaymentMethod;
import io.microprofile.tutorial.store.payment.entity.PaymentStatus;
import io.microprofile.tutorial.store.payment.repository.PaymentRepository;
import io.microprofile.tutorial.store.payment.client.OrderServiceClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Service class for Payment management operations.
 */
@ApplicationScoped
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    @Inject
    private PaymentRepository paymentRepository;
    
    @Inject
    private OrderServiceClient orderServiceClient;

    /**
     * Creates a new payment.
     *
     * @param payment The payment to create
     * @return The created payment
     */
    @Transactional
    public Payment createPayment(Payment payment) {
        // Set default values if not provided
        if (payment.getStatus() == null) {
            payment.setStatus(PaymentStatus.PENDING);
        }
        
        if (payment.getCreatedAt() == null) {
            payment.setCreatedAt(LocalDateTime.now());
        }
        
        payment.setUpdatedAt(LocalDateTime.now());
        
        // Generate a transaction reference if not provided
        if (payment.getTransactionReference() == null || payment.getTransactionReference().trim().isEmpty()) {
            payment.setTransactionReference(generateTransactionReference(payment.getPaymentMethod()));
        }
        
        LOGGER.info("Creating new payment: " + payment);
        return paymentRepository.save(payment);
    }

    /**
     * Gets a payment by ID.
     *
     * @param id The payment ID
     * @return The payment
     * @throws WebApplicationException if the payment is not found
     */
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException("Payment not found", Response.Status.NOT_FOUND));
    }

    /**
     * Gets payments by user ID.
     *
     * @param userId The user ID
     * @return A list of payments for the specified user
     */
    public List<Payment> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Gets payments by order ID.
     *
     * @param orderId The order ID
     * @return A list of payments for the specified order
     */
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    /**
     * Gets payments by status.
     *
     * @param status The payment status
     * @return A list of payments with the specified status
     */
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }

    /**
     * Gets all payments.
     *
     * @return A list of all payments
     */
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    /**
     * Updates a payment.
     *
     * @param id The payment ID
     * @param payment The updated payment information
     * @return The updated payment
     * @throws WebApplicationException if the payment is not found
     */
    @Transactional
    public Payment updatePayment(Long id, Payment payment) {
        Payment existingPayment = getPaymentById(id);
        
        payment.setPaymentId(id);
        payment.setCreatedAt(existingPayment.getCreatedAt());
        payment.setUpdatedAt(LocalDateTime.now());
        
        return paymentRepository.update(id, payment)
                .orElseThrow(() -> new WebApplicationException("Failed to update payment", Response.Status.INTERNAL_SERVER_ERROR));
    }

    /**
     * Updates a payment status.
     *
     * @param id The payment ID
     * @param status The new payment status
     * @return The updated payment
     * @throws WebApplicationException if the payment is not found
     */
    @Transactional
    public Payment updatePaymentStatus(Long id, PaymentStatus status) {
        Payment payment = getPaymentById(id);
        
        // Store old status to check for changes
        PaymentStatus oldStatus = payment.getStatus();
        
        payment.setStatus(status);
        payment.setUpdatedAt(LocalDateTime.now());
        
        Payment updatedPayment = paymentRepository.update(id, payment)
                .orElseThrow(() -> new WebApplicationException("Failed to update payment status", Response.Status.INTERNAL_SERVER_ERROR));
                
        // If the status changed to COMPLETED, update the order status
        if (status == PaymentStatus.COMPLETED && oldStatus != PaymentStatus.COMPLETED) {
            LOGGER.info("Payment completed, updating order status for order: " + payment.getOrderId());
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAID");
        } else if (status == PaymentStatus.FAILED && oldStatus != PaymentStatus.FAILED) {
            LOGGER.info("Payment failed, updating order status for order: " + payment.getOrderId());
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAYMENT_FAILED");
        } else if (status == PaymentStatus.REFUNDED && oldStatus != PaymentStatus.REFUNDED) {
            LOGGER.info("Payment refunded, updating order status for order: " + payment.getOrderId());
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "REFUNDED");
        } else if (status == PaymentStatus.CANCELLED && oldStatus != PaymentStatus.CANCELLED) {
            LOGGER.info("Payment cancelled, updating order status for order: " + payment.getOrderId());
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAYMENT_CANCELLED");
        }
        
        return updatedPayment;
    }

    /**
     * Processes a payment.
     * In a real application, this would involve communication with payment gateways.
     *
     * @param id The payment ID
     * @return The processed payment
     * @throws WebApplicationException if the payment is not found or is in an invalid state
     */
    @Transactional
    public Payment processPayment(Long id) {
        Payment payment = getPaymentById(id);
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new WebApplicationException("Payment is not in PENDING state", Response.Status.BAD_REQUEST);
        }
        
        // Simulate payment processing
        LOGGER.info("Processing payment: " + payment.getPaymentId());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setUpdatedAt(LocalDateTime.now());
        
        // For demo purposes, simulate payment success/failure based on the payment amount cents value
        // If the cents are 00, the payment will fail, otherwise it will succeed
        String amountString = payment.getAmount().toString();
        boolean paymentSuccess = !amountString.endsWith(".00");
        
        if (paymentSuccess) {
            LOGGER.info("Payment successful: " + payment.getPaymentId());
            payment.setStatus(PaymentStatus.COMPLETED);
            // Update order status
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAID");
        } else {
            LOGGER.info("Payment failed: " + payment.getPaymentId());
            payment.setStatus(PaymentStatus.FAILED);
            // Update order status
            orderServiceClient.updateOrderStatus(payment.getOrderId(), "PAYMENT_FAILED");
        }
        
        return paymentRepository.update(id, payment)
                .orElseThrow(() -> new WebApplicationException("Failed to process payment", Response.Status.INTERNAL_SERVER_ERROR));
    }

    /**
     * Deletes a payment.
     *
     * @param id The payment ID
     * @throws WebApplicationException if the payment is not found
     */
    @Transactional
    public void deletePayment(Long id) {
        boolean deleted = paymentRepository.deleteById(id);
        if (!deleted) {
            throw new WebApplicationException("Payment not found", Response.Status.NOT_FOUND);
        }
    }
    
    /**
     * Generates a transaction reference based on the payment method.
     *
     * @param paymentMethod The payment method
     * @return A unique transaction reference
     */
    private String generateTransactionReference(PaymentMethod paymentMethod) {
        String prefix;
        
        switch (paymentMethod) {
            case CREDIT_CARD:
                prefix = "CC";
                break;
            case DEBIT_CARD:
                prefix = "DC";
                break;
            case PAYPAL:
                prefix = "PP";
                break;
            case BANK_TRANSFER:
                prefix = "BT";
                break;
            case CRYPTO:
                prefix = "CR";
                break;
            case GIFT_CARD:
                prefix = "GC";
                break;
            default:
                prefix = "TX";
        }
        
        // Generate a unique identifier using a UUID
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        
        // Combine with a timestamp to ensure uniqueness
        LocalDateTime now = LocalDateTime.now();
        String timestamp = String.format("%d%02d%02d%02d%02d", 
                                        now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                                        now.getHour(), now.getMinute());
        
        return prefix + "-" + timestamp + "-" + uuid;
    }
}
