package com.checkout.payment.gateway.bank.client;

import com.checkout.payment.gateway.bank.dto.request.BankPaymentRequest;
import com.checkout.payment.gateway.bank.dto.response.BankPaymentResponse;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.bank.mapper.BankPaymentMapper;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class BankClientImpl implements BankClient {
  private static final String PAYMENTS_PATH = "/payments";

  private final RestTemplate restTemplate;
  private final String bankPaymentsUrl;
  private final BankPaymentMapper bankPaymentMapper;

  public BankClientImpl(RestTemplate restTemplate,
                        @Value("${bank.simulator.base.url}") String bankBaseUrl,
                        BankPaymentMapper bankPaymentMapper) {
    this.restTemplate = restTemplate;
    this.bankPaymentsUrl = bankBaseUrl + PAYMENTS_PATH;
    this.bankPaymentMapper = bankPaymentMapper;
  }

  @Override
  public PaymentStatus processPayment(PaymentRequest request) {
    try {
      BankPaymentRequest bankRequest = bankPaymentMapper.toRequest(request);
      ResponseEntity<BankPaymentResponse> response = restTemplate.postForEntity(
          bankPaymentsUrl,
          bankRequest,
          BankPaymentResponse.class
      );
      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        return response.getBody().authorized() ? PaymentStatus.AUTHORIZED : PaymentStatus.DECLINED;
      } else {
        throw new BankException("Acquiring bank provided a bad response.");
      }
    } catch (HttpServerErrorException.ServiceUnavailable e) {
      throw new BankUnavailableException("Acquiring bank is unreachable.", e);
    } catch (RestClientException e) {
      throw new BankUnavailableException("Acquiring bank is unreachable.", e);
    }

  }
}
