package com.checkout.payment.gateway.api.controller;

import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.api.mapper.ApiPaymentMapper;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.api.dto.response.PaymentResponse;
import com.checkout.payment.gateway.domain.service.PaymentGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayControllerTest {
  // Passes Luhn check so we use for authorized/declined flows.
  public static final String VALID_CARD_NUMBER =
      "2222405343248877";

  // Fails teh Luhn check so we use for validation rejection tests.
  public static final String INVALID_LUHN_CARD_NUMBER =
      "2222405343248158";

  @Mock private PaymentGatewayService paymentGatewayService;
  @Mock private ApiPaymentMapper apiPaymentMapper;

  private PaymentGatewayController controller;

  @BeforeEach
  void setUp() {
    controller = new PaymentGatewayController(paymentGatewayService, apiPaymentMapper);
  }

  // Process Payment #########################

  @Test
  void whenProcessPaymentSucceeds_thenReturns200WithAuthorizedResponse() {
    UUID id = UUID.randomUUID();
    PaymentRequest request = new PaymentRequest(VALID_CARD_NUMBER, 4, 2036, "GBP", 100L, "123");
    Payment payment = new Payment(id, PaymentStatus.AUTHORIZED, "8877", 4, 2036, "GBP", 100);
    PaymentResponse response = new PaymentResponse(id, PaymentStatus.AUTHORIZED, "8877", 4, 2036, "GBP", 100);

    when(paymentGatewayService.processPayment(request)).thenReturn(payment);
    when(apiPaymentMapper.toResponse(payment)).thenReturn(response);

    ResponseEntity<PaymentResponse> result = controller.processPayment(request);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(response, result.getBody());
  }

  @Test
  void whenProcessPaymentIsDeclined_thenReturns200WithDeclinedResponse() {
    UUID id = UUID.randomUUID();
    PaymentRequest request = new PaymentRequest(INVALID_LUHN_CARD_NUMBER, 4, 2036, "GBP", 100L, "123");
    Payment payment = new Payment(id, PaymentStatus.DECLINED, "8112", 4, 2036, "GBP", 100);
    PaymentResponse response = new PaymentResponse(id, PaymentStatus.DECLINED, "8112", 4, 2036, "GBP", 100);

    when(paymentGatewayService.processPayment(request)).thenReturn(payment);
    when(apiPaymentMapper.toResponse(payment)).thenReturn(response);

    ResponseEntity<PaymentResponse> result = controller.processPayment(request);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(PaymentStatus.DECLINED, result.getBody().status());
  }

  @Test
  void whenProcessPaymentIsCalled_thenServiceCalledOnce() {
    PaymentRequest request = new PaymentRequest(VALID_CARD_NUMBER, 4, 2036, "GBP", 100L, "123");
    Payment payment = new Payment(UUID.randomUUID(), PaymentStatus.AUTHORIZED, "8877", 4, 2036, "GBP", 100);
    when(paymentGatewayService.processPayment(request)).thenReturn(payment);
    when(apiPaymentMapper.toResponse(payment)).thenReturn(mock(PaymentResponse.class));

    controller.processPayment(request);

    verify(paymentGatewayService, times(1)).processPayment(request);
  }

  @Test
  void whenPaymentRequestValidationFails_thenPaymentValidationExceptionPropagates() {
    PaymentRequest request = new PaymentRequest(null, 0, 2020, "XXX", -1L, "");
    when(paymentGatewayService.processPayment(request))
        .thenThrow(new PaymentValidationException("Payment request deemed invalid.", List.of("Card number is required.")));

    assertThrows(PaymentValidationException.class,
        () -> controller.processPayment(request));
  }

  @Test
  void whenBankIsUnavailable_thenBankUnavailableExceptionPropagates() {
    PaymentRequest request = new PaymentRequest(VALID_CARD_NUMBER, 4, 2036, "GBP", 100L, "123");
    when(paymentGatewayService.processPayment(request))
        .thenThrow(new BankUnavailableException("Bank down", new RuntimeException()));

    assertThrows(BankUnavailableException.class,
        () -> controller.processPayment(request));
  }

  @Test
  void whenBankReturnsError_thenBankExceptionPropagates() {
    PaymentRequest request = new PaymentRequest(VALID_CARD_NUMBER, 4, 2036, "GBP", 100L, "123");
    when(paymentGatewayService.processPayment(request))
        .thenThrow(new BankException("Bad bank response"));

    assertThrows(BankException.class,
        () -> controller.processPayment(request));
  }

  // Get Payment by Id #########################

  @Test
  void whenPaymentExists_thenReturns200WithMappedResponse() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.AUTHORIZED, "8877", 4, 2025, "GBP", 100);
    PaymentResponse response = new PaymentResponse(id, PaymentStatus.AUTHORIZED, "8877", 4, 2025, "GBP", 100);

    when(paymentGatewayService.getPaymentById(id)).thenReturn(payment);
    when(apiPaymentMapper.toResponse(payment)).thenReturn(response);

    ResponseEntity<PaymentResponse> result = controller.getPaymentById(id);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertEquals(response, result.getBody());
  }

  @Test
  void whenPaymentDoesNotExist_thenPaymentNotFoundExceptionPropagates() {
    UUID id = UUID.randomUUID();
    when(paymentGatewayService.getPaymentById(id))
        .thenThrow(new PaymentNotFoundException(id));
    assertThrows(PaymentNotFoundException.class, () -> controller.getPaymentById(id));
  }
}