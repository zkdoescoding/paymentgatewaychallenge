package com.checkout.payment.gateway.domain.service;

import com.checkout.payment.gateway.bank.client.BankClient;
import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.repository.PaymentsRepository;
import com.checkout.payment.gateway.domain.validation.PaymentRequestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayServiceTest {

  @Mock private PaymentsRepository paymentsRepository;
  @Mock private PaymentRequestValidator paymentRequestValidator;
  @Mock private BankClient bankClient;

  private PaymentGatewayService service;

  @BeforeEach
  void setUp() {
    service = new PaymentGatewayService(paymentsRepository, paymentRequestValidator, bankClient);
  }

  // getting Payment By Id #####################

  @Test
  void whenAuthorizedPaymentExists_thenItCanBeRetrieved() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.AUTHORIZED, "8877", 4, 2025, "GBP", 100);
    when(paymentsRepository.get(id)).thenReturn(Optional.of(payment));

    Payment result = service.getPaymentById(id);

    assertEquals(payment, result);
    verify(paymentsRepository).get(id);
  }

  @Test
  void whenAuthorizedPaymentDoesNotExist_thenPaymentNotFoundExceptionIsThrown() {
    UUID id = UUID.randomUUID();
    when(paymentsRepository.get(id)).thenReturn(Optional.empty());
    assertThrows(PaymentNotFoundException.class, () -> service.getPaymentById(id));
  }

  // processing a Payment #####################

  @Test
  void whenBankAuthorizesPayment_thenAuthorizedPaymentIsStoredAndReturned() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(request)).thenReturn(PaymentStatus.AUTHORIZED);

    Payment result = service.processPayment(request);

    assertEquals(PaymentStatus.AUTHORIZED, result.status());
    assertNotNull(result.id());
    verify(paymentsRepository).add(result);
  }

  @Test
  void whenBankDeclinesPayment_thenDeclinedPaymentIsStoredAndReturned() {
    PaymentRequest request = new PaymentRequest("2222405343248158", 4, 2099, "GBP", 50L, "456");
    when(bankClient.processPayment(request)).thenReturn(PaymentStatus.DECLINED);

    Payment result = service.processPayment(request);

    assertEquals(PaymentStatus.DECLINED, result.status());
    verify(paymentsRepository).add(result);
  }

  @Test
  void whenPaymentIsProcessed_thenCardNumberIsMaskedToLastFour() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(request)).thenReturn(PaymentStatus.AUTHORIZED);

    Payment result = service.processPayment(request);

    assertEquals("8877", result.cardNumberLastFour());
  }

  @Test
  void whenPaymentRequestValidationFails_thenBankIsNeverCalled() {
    PaymentRequest request = new PaymentRequest(null, 0, 2020, "XXX", 1L, "");
    doThrow(new PaymentValidationException("Payment request invalid.", List.of("Card number is required.")))
        .when(paymentRequestValidator).validate(request);

    assertThrows(PaymentValidationException.class, () -> service.processPayment(request));
    verify(bankClient, never()).processPayment(any());
  }

  @Test
  void whenBankIsUnavailable_thenExceptionPropagatesAndNothingIsStored() {
    PaymentRequest request = new PaymentRequest("2222405343248870", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(request))
        .thenThrow(new BankUnavailableException("Bank down", new RuntimeException()));

    assertThrows(BankUnavailableException.class, () -> service.processPayment(request));
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenTwoPaymentsAreProcessed_thenEachHasUniqueId() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(any())).thenReturn(PaymentStatus.AUTHORIZED);

    Payment first = service.processPayment(request);
    Payment second = service.processPayment(request);

    assertNotEquals(first.id(), second.id());
  }

  @Test
  void whenPaymentIsRejected_thenNoPaymentWithRejectedStatusIsStored() {
    PaymentRequest request = new PaymentRequest(null, 0, 2020, "XXX", -1L, "");
    doThrow(new PaymentValidationException("Payment request deemed invalid.", List.of("Card number is required.")))
        .when(paymentRequestValidator).validate(request);

    assertThrows(PaymentValidationException.class, () -> service.processPayment(request));
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenBankReturnsABadResponse_thenBankExceptionPropagatesAndNothingIsStored() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(request))
        .thenThrow(new BankException("Acquiring bank provided a bad response."));

    assertThrows(BankException.class, () -> service.processPayment(request));
    verify(paymentsRepository, never()).add(any());
  }

  @Test
  void whenPaymentIsProcessed_thenAllFieldsAreCorrectlyPopulated() {
    PaymentRequest request = new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");
    when(bankClient.processPayment(request)).thenReturn(PaymentStatus.AUTHORIZED);

    Payment result = service.processPayment(request);

    assertNotNull(result.id());
    assertEquals(PaymentStatus.AUTHORIZED, result.status());
    assertEquals("8877", result.cardNumberLastFour());
    assertEquals(4, result.expiryMonth());
    assertEquals(2099, result.expiryYear());
    assertEquals("GBP", result.currency());
    assertEquals(100L, result.amount());
  }
}