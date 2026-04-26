package com.checkout.payment.gateway.domain.service;

import com.checkout.payment.gateway.bank.client.BankClient;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import java.util.UUID;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static com.checkout.payment.gateway.domain.model.PaymentConstants.CARD_LAST_DIGITS;

@Service
public class PaymentGatewayService {

  private static final Logger LOG = LoggerFactory.getLogger(PaymentGatewayService.class);

  private final PaymentsRepository paymentsRepository;
  private final PaymentRequestValidator paymentRequestValidator;
  private final BankClient acquiringBank;

  public PaymentGatewayService(PaymentsRepository paymentsRepository,
                                PaymentRequestValidator paymentRequestValidator,
                                BankClient acquiringBank) {
    this.paymentsRepository = paymentsRepository;
    this.paymentRequestValidator = paymentRequestValidator;
    this.acquiringBank = acquiringBank;
  }

  public Payment getPaymentById(UUID id) {
    LOG.debug("Requesting access to payment with id {}", id);
    return paymentsRepository.get(id).orElseThrow(() -> new PaymentNotFoundException(id));
  }

  public Payment processPayment(PaymentRequest request) {
    UUID paymentId = UUID.randomUUID();
    String lastFour = request.cardNumber() != null && request.cardNumber().length() >= CARD_LAST_DIGITS
        ? request.cardNumber().substring(request.cardNumber().length() - CARD_LAST_DIGITS)
        : "N/A";

    LOG.info("Processing payment. paymentId={} lastFour={} currency={} amount={}",
        paymentId, lastFour, request.currency(), request.amount());

    PaymentStatus authorizedStatus;
    try {
      paymentRequestValidator.validate(request);
      authorizedStatus = acquiringBank.processPayment(request);
    } catch (PaymentValidationException | BankException e) {
      LOG.warn("Payment rejected. paymentId={} lastFour={} reason={}",
          paymentId, lastFour, e.getMessage());
      throw e;
    } catch (Exception e) {
      LOG.error("An unexpected error occurred. paymentId={} lastFour={}",
          paymentId, lastFour, e);
      throw e;
    }

    Payment payment = buildPayment(request, paymentId, lastFour, authorizedStatus);
    paymentsRepository.add(payment);

    LOG.info("Payment processed. id={} status={} amount={} currency={} lastFour={}",
        payment.id(), payment.status(), payment.amount(), payment.currency(), payment.cardNumberLastFour());

    return payment;
  }

  private Payment buildPayment(PaymentRequest request, UUID id, String lastFour, PaymentStatus status) {
    return new Payment(id, status, lastFour, request.expiryMonth(), request.expiryYear(),
        request.currency(), request.amount());
  }


}
