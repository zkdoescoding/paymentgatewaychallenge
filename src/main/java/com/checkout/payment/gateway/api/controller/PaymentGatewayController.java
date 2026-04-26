package com.checkout.payment.gateway.api.controller;

import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.api.mapper.ApiPaymentMapper;
import com.checkout.payment.gateway.api.dto.response.PaymentResponse;
import com.checkout.payment.gateway.api.dto.request.PaymentRequest;
import com.checkout.payment.gateway.domain.service.PaymentGatewayService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for payment operations.
 *
 * <p>Exposes endpoints for executing different payment routines
 * via the payment gateway.
 */
@RestController
@RequestMapping("/api/v1")
public class PaymentGatewayController {

  private final PaymentGatewayService paymentGatewayService;
  private final ApiPaymentMapper apiPaymentMapper;

  public PaymentGatewayController(PaymentGatewayService paymentGatewayService,
                                  ApiPaymentMapper apiPaymentMapper) {
    this.paymentGatewayService = paymentGatewayService;
    this.apiPaymentMapper = apiPaymentMapper;
  }

  /**
   * Retrieves a previously processed payment by its unique identifier.
   *
   * @param id the UUID of the previously processed payment to retrieve
   * @return {@code 200 OK} with the payment details
   * @throws PaymentNotFoundException if no payment exists with the given ID
   */
  @GetMapping("/payments/{id}")
  public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID id) {
    Payment payment = paymentGatewayService.getPaymentById(id);
    return new ResponseEntity<>(apiPaymentMapper.toResponse(payment), HttpStatus.OK);
  }

  /**
   * Submits a new payment request for processing.
   *
   * @param request the body of the payment request.
   * @return {@code 200 OK} with the processed payment and its resulting status
   * @throws PaymentValidationException if the request fails validation
   * @throws BankUnavailableException   if the acquiring bank is unreachable
   * @throws BankException              if the acquiring bank returns an unexpected response
   */
  @PostMapping("/payments")
  public ResponseEntity<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
    Payment payment = paymentGatewayService.processPayment(request);
    return new ResponseEntity<>(apiPaymentMapper.toResponse(payment), HttpStatus.OK);
  }
}
