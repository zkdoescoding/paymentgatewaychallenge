package com.checkout.payment.gateway.api.mapper;

import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.api.dto.response.PaymentResponse;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultApiPaymentMapperTest {

  private final DefaultApiPaymentMapper mapper = new DefaultApiPaymentMapper();

  @Test
  void whenMappingPaymentToResponse_thenAllFieldsAreMappedCorrectly() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.AUTHORIZED, "8877", 4, 2025, "GBP", 100);

    PaymentResponse response = mapper.toResponse(payment);

    assertEquals(id, response.id());
    assertEquals(PaymentStatus.AUTHORIZED, response.status());
    assertEquals("8877", response.cardNumberLastFour());
    assertEquals(4, response.expiryMonth());
    assertEquals(2025, response.expiryYear());
    assertEquals("GBP", response.currency());
    assertEquals(100, response.amount());
  }

  @Test
  void whenMappingDeclinedPayment_thenStatusIsDeclined() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.DECLINED, "0012", 1, 2030, "EUR", 500);

    PaymentResponse response = mapper.toResponse(payment);

    assertEquals(PaymentStatus.DECLINED, response.status());
  }

  @Test
  void whenMappingRejectedPayment_thenStatusIsRejected() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.REJECTED, "0012", 1, 2030, "EUR", 500);

    PaymentResponse response = mapper.toResponse(payment);

    assertEquals(PaymentStatus.REJECTED, response.status());
  }
}