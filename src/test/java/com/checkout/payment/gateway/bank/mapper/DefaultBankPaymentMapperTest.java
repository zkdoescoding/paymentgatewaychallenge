package com.checkout.payment.gateway.bank.mapper;

import com.checkout.payment.gateway.bank.dto.request.BankPaymentRequest;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultBankPaymentMapperTest {

  private final DefaultBankPaymentMapper mapper = new DefaultBankPaymentMapper();

  @Test
  void whenMappingPaymentRequestToBankRequest_thenAllFieldsAreMappedCorrectly() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2025, "GBP", 100L, "123");

    BankPaymentRequest bankRequest = mapper.toRequest(request);

    assertEquals("2222405343248877", bankRequest.cardNumber());
    assertEquals("04/2025", bankRequest.expiryDate());
    assertEquals("GBP", bankRequest.currency());
    assertEquals(100, bankRequest.amount());
    assertEquals("123", bankRequest.cvv());
  }

  @Test
  void whenExpiryMonthIsSingleDigit_thenItIsPaddedWithLeadingZero() {
    PaymentRequest request = new PaymentRequest("12345678901234", 1, 2099, "USD", 1L, "999");

    BankPaymentRequest bankRequest = mapper.toRequest(request);

    assertEquals("01/2099", bankRequest.expiryDate());
  }
}