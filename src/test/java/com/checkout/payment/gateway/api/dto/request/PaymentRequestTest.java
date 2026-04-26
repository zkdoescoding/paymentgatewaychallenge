package com.checkout.payment.gateway.api.dto.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestTest {

  // Currency normalisation #############

  @ParameterizedTest
  @ValueSource(strings = {"usd", "gbp", "eur", "Usd", "GbP"})
  void whenCurrencyIsAnyCase_thenItIsNormalisedToUppercase(String currency) {
    var request = new PaymentRequest("12345678901234", 4, 2099, currency, 100L, "123");
    assertEquals(currency.toUpperCase(), request.currency());
  }

  @Test
  void whenCurrencyHasLeadingOrTrailingWhitespace_thenItIsTrimmed() {
    var request = new PaymentRequest("12345678901234", 4, 2099, "  GBP  ", 100L, "123");
    assertEquals("GBP", request.currency());
  }

  @Test
  void whenCurrencyIsNull_thenItRemainsNull() {
    assertDoesNotThrow(() -> new PaymentRequest("12345678901234", 4, 2099, null, 100L, "123"));
  }

  // expiryDate normalisation #############

  @Test
  void whenExpiryMonthIsTwoDigits_thenExpiryDateIsFormattedCorrectly() {
    var request = new PaymentRequest("12345678901234", 12, 2025, "GBP", 100L, "123");
    assertEquals("12/2025", request.expiryDate());
  }

  @Test
  void whenExpiryMonthIsSingleDigit_thenItIsPaddedWithLeadingZero() {
    var request = new PaymentRequest("12345678901234", 4, 2025, "GBP", 100L, "123");
    assertEquals("04/2025", request.expiryDate());
  }
}