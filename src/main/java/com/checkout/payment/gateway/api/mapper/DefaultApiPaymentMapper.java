package com.checkout.payment.gateway.api.mapper;

import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.api.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class DefaultApiPaymentMapper implements ApiPaymentMapper {
  @Override
  public PaymentResponse toResponse(Payment payment) {
    return new PaymentResponse(
        payment.id(),
        payment.status(),
        payment.cardNumberLastFour(),
        payment.expiryMonth(),
        payment.expiryYear(),
        payment.currency(),
        payment.amount()
    );
  }
}