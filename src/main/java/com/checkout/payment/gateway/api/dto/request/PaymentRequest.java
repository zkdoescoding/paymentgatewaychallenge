package com.checkout.payment.gateway.api.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Locale;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaymentRequest(
    String cardNumber,
    Integer  expiryMonth,
    Integer  expiryYear,
    String currency,
    Long amount,
    String cvv
) {
  public PaymentRequest {
    if (currency != null) {
      currency = currency.trim().toUpperCase(Locale.ROOT);
    }
  }

  public String expiryDate() {
    return String.format("%02d/%d", expiryMonth, expiryYear);
  }
}