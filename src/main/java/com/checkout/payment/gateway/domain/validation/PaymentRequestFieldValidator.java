package com.checkout.payment.gateway.domain.validation;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;


public interface PaymentRequestFieldValidator {
  ValidationResult validateField(PaymentRequest request);
}
