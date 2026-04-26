package com.checkout.payment.gateway.domain.exception;

import java.util.List;

public class PaymentValidationException extends RuntimeException {

  private final List<String> validationErrors;

  public PaymentValidationException(String message, List<String> validationErrors) {
    super(message);
    this.validationErrors = validationErrors;
  }

  public List<String> getValidationErrors() {
    return this.validationErrors;
  }
}
