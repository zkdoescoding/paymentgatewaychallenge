package com.checkout.payment.gateway.api.dto.response;

import com.checkout.payment.gateway.domain.model.PaymentStatus;
import java.util.List;

public record RejectedPaymentResponse(
    PaymentStatus status,
    List<String> errors
) {}