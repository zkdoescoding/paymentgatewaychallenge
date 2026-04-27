package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyValidatorTest {

  private final CurrencyValidator validator = new CurrencyValidator();

  private PaymentRequest requestWith(String currency) {
    return new PaymentRequest("12345678901234", 12, 2036, currency, 100L, "123");
  }

  @Test
  void whenCurrencyIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null));
    assertFalse(result.isValid());
    assertEquals("Currency is required", result.getMessage());
  }

  @Test
  void whenCurrencyIsBlank_thenValidationFails() {
    var result = validator.validateField(requestWith("   "));
    assertFalse(result.isValid());
    assertEquals("Currency is required", result.getMessage());
  }

  @Test
  void whenCurrencyIsTwoCharacters_thenValidationFails() {
    var result = validator.validateField(requestWith("US"));
    assertFalse(result.isValid());
    assertEquals("Currency must be 3 characters", result.getMessage());
  }

  @Test
  void whenCurrencyIsFourCharacters_thenValidationFails() {
    var result = validator.validateField(requestWith("USDD"));
    assertFalse(result.isValid());
    assertEquals("Currency must be 3 characters", result.getMessage());
  }

  @Test
  void whenCurrencyIsUnsupported_thenValidationFails() {
    var result = validator.validateField(requestWith("JPY"));
    assertFalse(result.isValid());
  }

  @ParameterizedTest
  @ValueSource(strings = {"USD", "GBP", "EUR"})
  void whenCurrencyIsSupported_thenValidationPasses(String currency) {
    var result = validator.validateField(requestWith(currency));
    assertTrue(result.isValid());
  }

  @ParameterizedTest
  @ValueSource(strings = {"usd", "gbp", "eur"})
  void whenCurrencyIsLowercaseSupported_thenValidationPasses(String currency) {
    var result = validator.validateField(requestWith(currency));
    assertTrue(result.isValid());
  }
}