package com.checkout.payment.gateway.integration.controller;

import com.checkout.payment.gateway.bank.client.BankClient;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentGatewayControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BankClient bankClient;

  private static final String VALID_REQUEST_PAYLOAD = """
      {
        "card_number": "2222405343248877",
        "expiry_month": 4,
        "expiry_year": 2036,
        "currency": "GBP",
        "amount": 100,
        "cvv": "123"
      }
      """;

  @Test
  void whenPaymentAuthorized_thenReturns200AndCanBeRetrieved() throws Exception {
    when(bankClient.processPayment(any())).thenReturn(PaymentStatus.AUTHORIZED);

    // POST process the payment
    MvcResult result = mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_REQUEST_PAYLOAD))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("8877"))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100))
        .andReturn();

    // extract id from response
    String id = objectMapper.readValue(
        result.getResponse().getContentAsString(), Map.class
    ).get("id").toString();

    // GET verify it was stored and can be retrieved
    mockMvc.perform(get("/api/v1/payments/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.status").value("Authorized"))
        .andExpect(jsonPath("$.card_number_last_four").value("8877"))
        .andExpect(jsonPath("$.expiry_month").value(4))
        .andExpect(jsonPath("$.expiry_year").value(2036))
        .andExpect(jsonPath("$.currency").value("GBP"))
        .andExpect(jsonPath("$.amount").value(100));
  }

  @Test
  void whenPaymentDeclined_thenReturns200WithDeclinedStatus() throws Exception {
    when(bankClient.processPayment(any())).thenReturn(PaymentStatus.DECLINED);

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(VALID_REQUEST_PAYLOAD))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Declined"))
        .andExpect(jsonPath("$.card_number_last_four").value("8877"));
  }

  @Test
  void whenRequestIsInvalid_thenReturns400RejectedAndBankNeverCalled() throws Exception {
    String invalidRequestPayload = """
        {
          "card_number": "123",
          "expiry_month": 4,
          "expiry_year": 2036,
          "currency": "GBP",
          "amount": 100,
          "cvv": "123"
        }
        """;

    mockMvc.perform(post("/api/v1/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidRequestPayload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value("Rejected"));

    verifyNoInteractions(bankClient);
  }

  @Test
  void whenPaymentNotFound_thenReturns404() throws Exception {
    mockMvc.perform(get("/api/v1/payments/{id}", UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Payment not found"));
  }
}
