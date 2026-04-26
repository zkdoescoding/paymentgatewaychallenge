package com.checkout.payment.gateway.bank.exception;

public class BankUnavailableException extends BankException {

  public BankUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
