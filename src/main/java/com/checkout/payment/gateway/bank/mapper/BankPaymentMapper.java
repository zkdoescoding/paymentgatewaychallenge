package com.checkout.payment.gateway.bank.mapper;

import com.checkout.payment.gateway.bank.dto.request.BankPaymentRequest;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;

public interface BankPaymentMapper {
  BankPaymentRequest toRequest(PaymentRequest request);
}