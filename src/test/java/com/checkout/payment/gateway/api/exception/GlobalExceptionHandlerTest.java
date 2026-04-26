package com.checkout.payment.gateway.api.exception;

import com.checkout.payment.gateway.api.controller.PaymentGatewayController;
import com.checkout.payment.gateway.api.mapper.ApiPaymentMapper;
import com.checkout.payment.gateway.domain.service.PaymentGatewayService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(PaymentGatewayController.class)
class GlobalExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PaymentGatewayService paymentGatewayService;

  @MockBean
  private ApiPaymentMapper apiPaymentMapper;

  private static final String VALID_PAYMENT_JSON = """
      {
        "card_number": "2222405343248877",
        "expiry_month": 4,
        "expiry_year": 2099,
        "currency": "GBP",
        "amount": 100,
        "cvv": "123"
      }
      """;

  // Bad Request #######################

  @Test
  void whenJsonBodyIsMalformed_thenBadRequestIsReturned() throws Exception {
    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid request"));
  }

  @Test
  void whenPaymentIdIsNotAValidUUID_thenBadRequestIsReturned() throws Exception {
    mockMvc.perform(get("/api/v1/payments/not-uuid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid request"));
  }

  // Not Found #######################

  @Test
  void whenPaymentIsNotFound_then404NotFoundIsReturned() throws Exception {
    when(paymentGatewayService.getPaymentById(any(UUID.class)))
        .thenThrow(new PaymentNotFoundException(UUID.randomUUID()));

    mockMvc.perform(get("/api/v1/payments/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment not found"));
  }

  // Rejected #######################

  @Test
  void whenPaymentValidationFails_thenBadRequestWithRejectedStatusAndErrorsIsReturned() throws Exception {
    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new PaymentValidationException("Payment request deemed invalid.", List.of("Card number is required.")));

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_PAYMENT_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"))
        .andExpect(jsonPath("$.errors[0]").value("Card number is required."));
  }

  // Service Unavailable #######################

  @Test
  void whenBankIsUnavailable_thenServiceUnavailableIsReturned() throws Exception {
    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new BankUnavailableException("Acquiring Bank is down", new RuntimeException()));

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_PAYMENT_JSON))
        .andExpect(status().isServiceUnavailable())
        .andExpect(jsonPath("$.message").value("Payment processor unavailable"));
  }

  // Bad Gateway #######################

  @Test
  void whenBankReturnsAnError_thenBadGatewayIsReturned() throws Exception {
    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new BankException("Acquiring bank provided a bad response."));

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_PAYMENT_JSON))
        .andExpect(status().isBadGateway())
        .andExpect(jsonPath("$.message").value("Payment processing error"));
  }

  // Internal Server Error #######################

  @Test
  void whenAnUnexpectedExceptionOccurs_thenInternalServerErrorIsReturned() throws Exception {
    when(paymentGatewayService.processPayment(any()))
        .thenThrow(new RuntimeException("An unexpected exception occurred"));

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_PAYMENT_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.message").value("Internal server error"));
  }
}