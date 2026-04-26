package com.checkout.payment.gateway.bank.client;

import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;

public interface BankClient {

  public PaymentStatus processPayment(PaymentRequest request);
}
