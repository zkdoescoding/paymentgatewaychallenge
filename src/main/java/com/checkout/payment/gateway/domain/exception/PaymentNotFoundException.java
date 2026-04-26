package com.checkout.payment.gateway.domain.exception;

import java.util.UUID;

public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException(UUID id) {
      super("No payment found with ID: " + id);
    }
}
