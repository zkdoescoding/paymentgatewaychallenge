package com.checkout.payment.gateway.domain.model;

import java.util.Set;

public final class PaymentConstants {

  private PaymentConstants() {}

  // Card number
  public static final int CARD_NUMBER_MIN_LENGTH = 14;
  public static final int CARD_NUMBER_MAX_LENGTH = 19;
  public static final int CARD_LAST_DIGITS = 4;

  // CVV
  public static final int CVV_MIN_LENGTH = 3;
  public static final int CVV_MAX_LENGTH = 4;

  // Currency
  public static final Set<String> SUPPORTED_CURRENCIES = Set.of("USD", "GBP", "EUR");
  public static final int ISO_4217_CODE_LENGTH = 3;

  // Expiry month
  public static final int MIN_EXPIRY_MONTH = 1;
  public static final int MAX_EXPIRY_MONTH = 12;
}
