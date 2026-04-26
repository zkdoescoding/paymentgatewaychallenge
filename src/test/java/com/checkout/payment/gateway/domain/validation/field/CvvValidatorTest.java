package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CvvValidatorTest {

  private final CvvValidator validator = new CvvValidator();

  private PaymentRequest requestWith(String cvv) {
    return new PaymentRequest("12345678901234", 12, 2099, "USD", 100L, cvv);
  }

  @Test
  void whenCvvIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null));
    assertFalse(result.isValid());
    assertEquals("CVV is required.", result.getMessage());
  }

  @Test
  void whenCvvIsBlank_thenValidationFails() {
    var result = validator.validateField(requestWith("   "));
    assertFalse(result.isValid());
    assertEquals("CVV is required.", result.getMessage());
  }

  @Test
  void whenCvvContainsLetters_thenValidationFails() {
    var result = validator.validateField(requestWith("12a"));
    assertFalse(result.isValid());
    assertEquals("CVV must only contain numeric characters.", result.getMessage());
  }

  @Test
  void whenCvvHasTwoDigits_thenValidationFails() {
    var result = validator.validateField(requestWith("12"));
    assertFalse(result.isValid());
    assertEquals("CVV must be between 3 or 4 digits long.", result.getMessage());
  }

  @Test
  void whenCvvHasFiveDigits_thenValidationFails() {
    var result = validator.validateField(requestWith("12345"));
    assertFalse(result.isValid());
    assertEquals("CVV must be between 3 or 4 digits long.", result.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"123", "1234"})
  void whenCvvIsThreeOrFourDigits_thenValidationPasses(String cvv) {
    var result = validator.validateField(requestWith(cvv));
    assertTrue(result.isValid());
  }
}