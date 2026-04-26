package com.checkout.payment.gateway.bank.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankHealthIndicatorTest {

  @Mock
  private RestTemplate restTemplate;

  private BankHealthIndicator indicator;

  private static final String BANK_URL = "http://localhost:8080";

  @BeforeEach
  void setUp() {
    indicator = new BankHealthIndicator(restTemplate, BANK_URL);
  }

  @Test
  void whenBankReturns2xx_thenStatusIsUp() {
    when(restTemplate.getForEntity(eq(BANK_URL), any()))
        .thenReturn(ResponseEntity.ok(""));

    Health health = indicator.health();

    assertEquals(Status.UP, health.getStatus());
    assertEquals("reachable", health.getDetails().get("acquiring bank"));
  }

  @Test
  void whenBankReturnsHttpError_thenStatusIsStillUp() {
    when(restTemplate.getForEntity(eq(BANK_URL), any()))
        .thenThrow(new HttpServerErrorException(HttpStatus.SERVICE_UNAVAILABLE));

    Health health = indicator.health();

    assertEquals(Status.UP, health.getStatus());
    assertEquals("reachable", health.getDetails().get("acquiring bank"));
  }

  @Test
  void whenBankIsUnreachable_thenStatusIsDown() {
    when(restTemplate.getForEntity(eq(BANK_URL), any()))
        .thenThrow(new ResourceAccessException("Connection refused"));

    Health health = indicator.health();

    assertEquals(Status.DOWN, health.getStatus());
    assertEquals("unreachable", health.getDetails().get("acquiring bank"));
  }
}
