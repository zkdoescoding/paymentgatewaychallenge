package com.checkout.payment.gateway.domain.validation;

import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentRequestValidator {

  private final List<PaymentRequestFieldValidator> validators;

  public PaymentRequestValidator(List<PaymentRequestFieldValidator> validators) {
    this.validators = validators;
  }

  public void validate(PaymentRequest request) {
    if (request == null) {
      throw new PaymentValidationException("Payment request deemed invalid.", List.of("Invalid payment request."));
    }

    List<String> validationErrors = validators.stream()
            .map(v -> v.validateField(request))
            .filter(ValidationResult::isInvalid)
            .map(ValidationResult::getMessage)
            .toList();

    if (!validationErrors.isEmpty()) {
      throw new PaymentValidationException("Payment request deemed invalid.", validationErrors);
    }
  }
}
