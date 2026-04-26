package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.PaymentRequestFieldValidator;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.checkout.payment.gateway.domain.model.PaymentConstants.CVV_MAX_LENGTH;
import static com.checkout.payment.gateway.domain.model.PaymentConstants.CVV_MIN_LENGTH;

@Component
@Order(4)
public class CvvValidator implements PaymentRequestFieldValidator {

  @Override
  public ValidationResult validateField(PaymentRequest request) {
    String cvv = request.cvv();

    if (cvv == null || cvv.isBlank()) {
      return ValidationResult.invalid("CVV is required.");
    }
    if (!cvv.matches("\\d+")) {
      return ValidationResult.invalid("CVV must only contain numeric characters.");
    }
    if (cvv.length() < CVV_MIN_LENGTH || cvv.length() > CVV_MAX_LENGTH) {
      return ValidationResult.invalid("CVV must be between 3 or 4 digits long.");
    }

    return ValidationResult.valid();
  }
}