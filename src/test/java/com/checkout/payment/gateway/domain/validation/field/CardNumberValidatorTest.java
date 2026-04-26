package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CardNumberValidatorTest {

  private final CardNumberValidator validator = new CardNumberValidator();

  private PaymentRequest requestWith(String cardNumber) {
    return new PaymentRequest(cardNumber, 12, 2099, "USD", 100L, "123");
  }

  @Test
  void whenCardNumberIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null));
    assertFalse(result.isValid());
    assertEquals("Card number is required.", result.getMessage());
  }

  @Test
  void whenCardNumberIsBlank_thenValidationFails() {
    var result = validator.validateField(requestWith("   "));
    assertFalse(result.isValid());
    assertEquals("Card number is required.", result.getMessage());
  }

  @Test
  void whenCardNumberContainsLetters_thenValidationFails() {
    var result = validator.validateField(requestWith("1234abc789012345"));
    assertFalse(result.isValid());
    assertEquals("Card number must only contain numeric characters.", result.getMessage());
  }

  @Test
  void whenCardNumberContainsSpecialCharacters_thenValidationFails() {
    var result = validator.validateField(requestWith("1234-5678-9012-3456"));
    assertFalse(result.isValid());
    assertEquals("Card number must only contain numeric characters.", result.getMessage());
  }

  @Test
  void whenCardNumberHas13Digits_thenValidationFails() {
    var result = validator.validateField(requestWith("1234567890123")); // 13
    assertFalse(result.isValid());
    assertEquals("Card number must be between 14-19 digits long.", result.getMessage());
  }

  @Test
  void whenCardNumberHas20Digits_thenValidationFails() {
    var result = validator.validateField(requestWith("12345678901234567890")); // 20
    assertFalse(result.isValid());
    assertEquals("Card number must be between 14-19 digits long.", result.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "12345678901237",       // 14 len, min length
      "1234567890123452",     // 16 len, acceptable
      "1234567890123456785"   // 19 len, max length
  })
  void whenCardNumberLengthIsWithinBounds_thenValidationPasses(String cardNumber) {
    var result = validator.validateField(requestWith(cardNumber));
    assertTrue(result.isValid());
  }

  @Test
  void whenCardNumberPassesLuhnCheck_thenValidationResultIsValid() {
    // Luhn sum = 60
    PaymentRequest request = new PaymentRequest("2222405343248117", 4, 2099, "GBP", 100L, "123");

    ValidationResult result = validator.validateField(request);

    assertTrue(result.isValid());
  }

  @Test
  void whenCardNumberFailsLuhnCheck_thenValidationResultIsInvalid() {
    // Luhn sum = 55
    PaymentRequest request = new PaymentRequest("2222405343248112", 4, 2099, "GBP", 100L, "123");

    ValidationResult result = validator.validateField(request);

    assertTrue(result.isInvalid());
    assertEquals("Card number is invalid.", result.getMessage());
  }
}