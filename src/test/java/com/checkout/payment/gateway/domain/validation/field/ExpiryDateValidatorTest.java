package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

class ExpiryDateValidatorTest {

  private final ExpiryDateValidator validator = new ExpiryDateValidator();

  private PaymentRequest requestWith(Integer month, Integer year) {
    return new PaymentRequest("12345678901234", month, year, "USD", 100L, "123");
  }

  // Null fields  #########################

  @Test
  void whenExpiryMonthIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null, 2036));
    assertFalse(result.isValid());
    assertEquals("Card expiry month is required.", result.getMessage());
  }

  @Test
  void whenExpiryYearIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(6, null));
    assertFalse(result.isValid());
    assertEquals("Card expiry year is required.", result.getMessage());
  }

  @Test
  void whenBothExpiryMonthAndYearAreNull_thenValidationFails() {
    var result = validator.validateField(requestWith(null, null));
    assertFalse(result.isValid());
    assertEquals("Card expiry month is required. Card expiry year is required.", result.getMessage());
  }

  // Month only invalids  #########################

  @Test
  void whenExpiryMonthIsZero_thenValidationFails() {
    var result = validator.validateField(requestWith(0, 2036));
    assertFalse(result.isValid());
    assertEquals("Card expiry month must be between 1 and 12.", result.getMessage());
  }

  @Test
  void whenExpiryMonthIs13_thenValidationFails() {
    var result = validator.validateField(requestWith(13, 2036));
    assertFalse(result.isValid());
    assertEquals("Card expiry month must be between 1 and 12.", result.getMessage());
  }

  // Both month and year individually invalid  #########################

  @Test
  void whenBothExpiryMonthAndYearAreInvalid_thenValidationFails() {
    var result = validator.validateField(requestWith(0, 0));
    assertFalse(result.isValid());
    assertEquals(
        "Card expiry month must be between 1 and 12.",
        result.getMessage()
    );
  }

  @Test
  void whenExpiryMonthIsOutOfRangeAndYearIsNull_thenValidationFails() {
    var result = validator.validateField(requestWith(13, null));
    assertFalse(result.isValid());
    assertEquals(
        "Card expiry month must be between 1 and 12. Card expiry year is required.",
        result.getMessage()
    );
  }

  // Combined date invalids  #########################

  @Test
  void whenExpiryYearIsInThePast_thenValidationFails() {
    var result = validator.validateField(requestWith(1, 2020));
    assertFalse(result.isValid());
    assertEquals("Card expiry date must be in the future.", result.getMessage());
  }

  @Test
  void whenOnlyExpiryYearIsZero_thenValidationFailsWithFutureMessage() {
    var result = validator.validateField(requestWith(6, 0));
    assertFalse(result.isValid());
    assertEquals("Card expiry date must be in the future.", result.getMessage());
  }

  @Test
  void whenExpiryDateIsCurrentMonthAndYear_thenValidationFails() {
    YearMonth now = YearMonth.now(ZoneId.of("UTC"));
    var result = validator.validateField(requestWith(now.getMonthValue(), now.getYear()));
    assertFalse(result.isValid());
    assertEquals("Card expiry date must be in the future.", result.getMessage());
  }

  // Happy paths #########################

  @Test
  void whenExpiryDateIsNextMonth_thenValidationPasses() {
    YearMonth next = YearMonth.now(ZoneId.of("UTC")).plusMonths(1);
    var result = validator.validateField(requestWith(next.getMonthValue(), next.getYear()));
    assertTrue(result.isValid());
  }

  @Test
  void whenExpiryDateIsFarInTheFuture_thenValidationPasses() {
    var result = validator.validateField(requestWith(12, 2036));
    assertTrue(result.isValid());
  }
}