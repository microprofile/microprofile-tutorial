package io.microprofile.tutorial.store.payment.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment class for the microprofile tutorial store application.
 * This class represents a payment transaction in the system.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    private Long paymentId;

    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Amount cannot be null")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private BigDecimal amount;

    @NotNull(message = "Status cannot be null")
    private PaymentStatus status;
    
    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;
    
    private String transactionReference;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    private String paymentDetails; // JSON string with payment method specific details
}
