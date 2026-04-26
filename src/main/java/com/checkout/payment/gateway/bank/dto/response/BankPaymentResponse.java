package com.checkout.payment.gateway.bank.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record BankPaymentResponse(
    boolean authorized,
    String authorizationCode
) {}
