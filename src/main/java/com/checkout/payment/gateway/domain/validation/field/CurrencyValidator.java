package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.PaymentRequestFieldValidator;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.util.Set;

import static com.checkout.payment.gateway.domain.model.PaymentConstants.ISO_4217_CODE_LENGTH;
import static com.checkout.payment.gateway.domain.model.PaymentConstants.SUPPORTED_CURRENCIES;

@Component
@Order(3)
public class CurrencyValidator implements PaymentRequestFieldValidator {

  @Override
  public ValidationResult validateField(PaymentRequest request) {
    String currency = request.currency();

    if (currency == null || currency.isBlank()) {
      return ValidationResult.invalid("Currency is required");
    }
    if (currency.length() != ISO_4217_CODE_LENGTH) {
      return ValidationResult.invalid(
          String.format(
              "Currency must be %d characters",
              ISO_4217_CODE_LENGTH
          )
      );
    }
    if (!SUPPORTED_CURRENCIES.contains(currency)) {
      return ValidationResult.invalid(
          "Currency must be one of the supported currencies: " + SUPPORTED_CURRENCIES
      );
    }

    return ValidationResult.valid();
  }
}