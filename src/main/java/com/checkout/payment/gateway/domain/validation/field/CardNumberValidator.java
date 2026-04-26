package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.PaymentRequestFieldValidator;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.checkout.payment.gateway.domain.model.PaymentConstants.CARD_NUMBER_MAX_LENGTH;
import static com.checkout.payment.gateway.domain.model.PaymentConstants.CARD_NUMBER_MIN_LENGTH;
import static java.lang.Character.getNumericValue;

@Component
@Order(1)
public class CardNumberValidator implements PaymentRequestFieldValidator {

  @Override
  public ValidationResult validateField(PaymentRequest request) {
    String cardNumber = request.cardNumber();

    if (cardNumber == null || cardNumber.isBlank()) {
      return ValidationResult.invalid("Card number is required.");
    }
    if (!cardNumber.matches("\\d+")) {
      return ValidationResult.invalid("Card number must only contain numeric characters.");
    }
    if (cardNumber.length() < CARD_NUMBER_MIN_LENGTH || cardNumber.length() > CARD_NUMBER_MAX_LENGTH) {
      return ValidationResult.invalid(String.format("Card number must be between %d-%d digits long.", CARD_NUMBER_MIN_LENGTH, CARD_NUMBER_MAX_LENGTH));
    }
    if (!passesLuhnCheck(cardNumber))
      return ValidationResult.invalid("Card number is invalid.");

    return ValidationResult.valid();
  }

  private boolean passesLuhnCheck(String cardNumber) { // followed geeksforgeeks implementation for this
    int sum = 0;
    boolean isEvenPositionDigit = false;

    for (int i = cardNumber.length() - 1; i >= 0; i--) {
      int digit = getNumericValue(cardNumber.charAt(i));

      if (isEvenPositionDigit) {
        digit *= 2;
      }

      sum += digit / 10;
      sum += digit % 10;
      isEvenPositionDigit = !isEvenPositionDigit;
    }

    return sum % 10 == 0;
  }
}