package io.microprofile.tutorial.store.payment.exception;

public class CriticalPaymentException extends Exception {
    public CriticalPaymentException(String message) {
        super(message);
    }
}