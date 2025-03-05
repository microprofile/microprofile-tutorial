package io.microprofile.tutorial.store.payment.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {
    private String cardNumber;
    private String cardHolderName;
    private String expirationDate; // Format MM/YY
    private String securityCode;
    private BigDecimal amount;
}