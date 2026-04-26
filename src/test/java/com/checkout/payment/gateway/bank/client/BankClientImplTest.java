package com.checkout.payment.gateway.bank.client;

import com.checkout.payment.gateway.bank.dto.response.BankPaymentResponse;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.bank.mapper.BankPaymentMapper;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankClientImplTest {
  private static final String BANK_BASE_URL = "http://localhost:8080";

  @Mock private RestTemplate restTemplate;
  @Mock private BankPaymentMapper bankPaymentMapper;

  private BankClientImpl bankClient;

  private final PaymentRequest validRequest =
      new PaymentRequest("2222405343248877", 4, 2099, "GBP", 100L, "123");

  @BeforeEach
  void setUp() {
    bankClient = new BankClientImpl(restTemplate, BANK_BASE_URL, bankPaymentMapper);
  }

  // Bank simulator AUTHORISED ###################

  @Test
  void whenBankReturnsAuthorizedTrue_thenPaymentStatusIsAuthorized() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(true, "auth-code-123");
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenReturn(ResponseEntity.ok(bankResponse));

    PaymentStatus result = bankClient.processPayment(validRequest);

    assertEquals(PaymentStatus.AUTHORIZED, result);
  }

  // Bank simulator DECLINED ###################

  @Test
  void whenBankReturnsAuthorizedFalse_thenPaymentStatusIsDeclined() {
    BankPaymentResponse bankResponse = new BankPaymentResponse(false, null);
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenReturn(ResponseEntity.ok(bankResponse));

    PaymentStatus result = bankClient.processPayment(validRequest);

    assertEquals(PaymentStatus.DECLINED, result);
  }

  // Bank simulator 503 Service Unavailable ###################

  @Test
  void whenBankReturns503_thenBankUnavailableExceptionIsThrown() {
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenThrow(HttpServerErrorException.ServiceUnavailable.class);

    assertThrows(BankUnavailableException.class,
        () -> bankClient.processPayment(validRequest));
  }

  // Other RestClientExceptions like network failure, timeout etc  ###################

  @Test
  void whenRestClientExceptionOccurs_thenBankUnavailableExceptionIsThrown() {
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenThrow(new RestClientException("Connection refused"));

    assertThrows(BankUnavailableException.class,
        () -> bankClient.processPayment(validRequest));
  }

  // Other bad response ###################

  @Test
  void whenBankResponseBodyIsNull_thenBankExceptionIsThrown() {
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenReturn(ResponseEntity.ok(null));

    assertThrows(BankException.class,
        () -> bankClient.processPayment(validRequest));
  }

  @Test
  void whenBankReturnsNon2xxResponse_thenBankExceptionIsThrown() {
    when(restTemplate.postForEntity(anyString(), any(), eq(BankPaymentResponse.class)))
        .thenReturn(ResponseEntity.badRequest().build());

    assertThrows(BankException.class,
        () -> bankClient.processPayment(validRequest));
  }
}