package com.checkout.payment.gateway.bank.mapper;

import com.checkout.payment.gateway.bank.dto.request.BankPaymentRequest;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.springframework.stereotype.Component;

@Component
public class DefaultBankPaymentMapper implements BankPaymentMapper {

  @Override
  public BankPaymentRequest toRequest(PaymentRequest request) {
    return new BankPaymentRequest(
        request.cardNumber(),
        request.expiryDate(),
        request.currency(),
        request.amount(),
        request.cvv()
    );
  }
}
