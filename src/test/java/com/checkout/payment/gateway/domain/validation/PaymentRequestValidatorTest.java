package com.checkout.payment.gateway.domain.validation;

import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentRequestValidatorTest {

  @Test
  void whenPaymentRequestIsNull_thenValidationExceptionIsThrown() {
    var validator = new PaymentRequestValidator(List.of());
    var ex = assertThrows(PaymentValidationException.class, () -> validator.validate(null));
    assertFalse(ex.getValidationErrors().isEmpty());
  }

  @Test
  void whenPaymentRequestValid_thenNoExceptionsAreThrown() {
    PaymentRequestFieldValidator passing = r -> ValidationResult.valid();
    var validator = new PaymentRequestValidator(List.of(passing, passing));
    var request = new PaymentRequest("12345678901234", 12, 2099, "USD", 100L, "123");
    assertDoesNotThrow(() -> validator.validate(request));
  }

  @Test
  void whenOneFieldInvalid_thenExceptionContainsOnlyThatError() {
    PaymentRequestFieldValidator failing = r -> ValidationResult.invalid("Card number is required.");
    var validator = new PaymentRequestValidator(List.of(failing));
    var request = new PaymentRequest(null, 12, 2099, "USD", 100L, "123");

    var ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
    assertEquals(1, ex.getValidationErrors().size());
    assertTrue(ex.getValidationErrors().contains("Card number is required."));
  }

  @Test
  void whenMultipleFieldsInvalid_thenAllErrorsAreCollected() {
    PaymentRequestFieldValidator fail1 = r -> ValidationResult.invalid("Error 1");
    PaymentRequestFieldValidator fail2 = r -> ValidationResult.invalid("Error 2");
    PaymentRequestFieldValidator pass  = r -> ValidationResult.valid();
    var validator = new PaymentRequestValidator(List.of(fail1, pass, fail2));
    var request = new PaymentRequest("12345678901234", 12, 2099, "USD", 100L, "123");

    var ex = assertThrows(PaymentValidationException.class, () -> validator.validate(request));
    assertEquals(2, ex.getValidationErrors().size());
    assertTrue(ex.getValidationErrors().containsAll(List.of("Error 1", "Error 2")));
  }
}