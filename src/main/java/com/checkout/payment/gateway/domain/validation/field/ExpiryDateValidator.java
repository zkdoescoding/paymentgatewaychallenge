package com.checkout.payment.gateway.domain.validation.field;

import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.validation.PaymentRequestFieldValidator;
import com.checkout.payment.gateway.domain.validation.ValidationResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static com.checkout.payment.gateway.domain.model.PaymentConstants.MAX_EXPIRY_MONTH;
import static com.checkout.payment.gateway.domain.model.PaymentConstants.MIN_EXPIRY_MONTH;

@Component
@Order(2)
public class ExpiryDateValidator implements PaymentRequestFieldValidator {

  @Override
  public ValidationResult validateField(PaymentRequest request) {
    Integer month = request.expiryMonth();
    Integer year = request.expiryYear();

    List<String> errors = new ArrayList<>();

    if (month == null) {
      errors.add("Card expiry month is required.");
    } else if (month < MIN_EXPIRY_MONTH || month > MAX_EXPIRY_MONTH) {
      errors.add(
          String.format(
            "Card expiry month must be between %d and %d.",
              MIN_EXPIRY_MONTH, MAX_EXPIRY_MONTH
          )
      );
    }

    if (year == null) {
      errors.add("Card expiry year is required.");
    }

    if (!errors.isEmpty()) {
      return ValidationResult.invalid(String.join(" ", errors));
    }

    YearMonth expiry;
    try {
      expiry = YearMonth.of(year, month);
    } catch (DateTimeException e) {
      return ValidationResult.invalid("Invalid card expiry date.");
    }

    if (!expiry.isAfter(YearMonth.now(ZoneId.of("UTC")))) {
      return ValidationResult.invalid("Card expiry date must be in the future.");
    }

    return ValidationResult.valid();
  }
}