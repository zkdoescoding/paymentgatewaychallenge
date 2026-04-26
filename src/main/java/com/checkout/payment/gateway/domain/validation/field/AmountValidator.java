package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.PaymentRequestFieldValidator;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class AmountValidator implements PaymentRequestFieldValidator {

  @Override
  public ValidationResult validateField(PaymentRequest request) {
    if (request.amount() == null) {
      return ValidationResult.invalid("Amount is required.");
    }
    if (request.amount() <= 0) {
      return ValidationResult.invalid("Amount must be greater than zero.");
    }
    return ValidationResult.valid();
  }
}