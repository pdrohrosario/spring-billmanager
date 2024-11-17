package com.api.billmanager.domain.exception;

public class PaymentDateException extends RuntimeException {
    public PaymentDateException(String message) {
        super(message);
    }
}
