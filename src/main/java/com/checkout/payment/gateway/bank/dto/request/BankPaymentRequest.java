package com.checkout.payment.gateway.bank.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BankPaymentRequest(
    String cardNumber,
    String expiryDate,
    String currency,
    long amount,
    String cvv
) {}