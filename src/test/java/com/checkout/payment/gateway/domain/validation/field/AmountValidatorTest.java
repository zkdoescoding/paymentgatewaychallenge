package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AmountValidatorTest {

  private final AmountValidator validator = new AmountValidator();

  private PaymentRequest requestWith(Long amount) {
    return new PaymentRequest("12345678901234", 12, 2036, "USD", amount, "123");
  }

  @Test
  void whenAmountIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null));
    assertFalse(result.isValid());
    assertEquals("Amount is required.", result.getMessage());
  }

  @Test
  void whenAmountIsZero_thenValidationFails() {
    var result = validator.validateField(requestWith(0L));
    assertFalse(result.isValid());
    assertEquals("Amount must be greater than zero.", result.getMessage());
  }

  @Test
  void whenAmountIsNegative_thenValidationFails() {
    var result = validator.validateField(requestWith(-100L));
    assertFalse(result.isValid());
    assertEquals("Amount must be greater than zero.", result.getMessage());
  }

  @Test
  void whenAmountIsOne_thenValidationPasses() {
    var result = validator.validateField(requestWith(1L));
    assertTrue(result.isValid());
  }

  @Test
  void whenAmountIsLarge_thenValidationPasses() {
    var result = validator.validateField(requestWith(105000000L));
    assertTrue(result.isValid());
  }
}