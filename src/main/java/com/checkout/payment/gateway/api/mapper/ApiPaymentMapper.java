package com.checkout.payment.gateway.api.mapper;


import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.api.dto.response.PaymentResponse;

public interface ApiPaymentMapper {
  PaymentResponse toResponse(Payment payment);
}
