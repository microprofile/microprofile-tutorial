package io.microprofile.tutorial.store.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate; // Format MM/YY
    private String securityCode;
    private BigDecimal amount;
}
