package io.microprofile.tutorial.store.payment.exception;

public class PaymentProcessingException extends Exception {
    public PaymentProcessingException(String message) {
        super(message);
    }
}