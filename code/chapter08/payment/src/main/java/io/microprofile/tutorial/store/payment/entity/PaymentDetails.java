package io.microprofile.tutorial.store.payment.entity;

import java.math.BigDecimal;

public class PaymentDetails {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate; // Format MM/YY
    private String securityCode;
    private BigDecimal amount;

    public PaymentDetails() {
    }

    public PaymentDetails(String cardNumber, String cardHolderName, String expiryDate, String securityCode, BigDecimal amount) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryDate = expiryDate;
        this.securityCode = securityCode;
        this.amount = amount;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
