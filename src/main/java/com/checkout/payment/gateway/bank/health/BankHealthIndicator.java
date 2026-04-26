package com.checkout.payment.gateway.bank.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
@Component
public class BankHealthIndicator implements HealthIndicator {
  private final RestTemplate restTemplate;
  private final String bankBaseUrl;
  public BankHealthIndicator(RestTemplate restTemplate,
      @Value("${bank.simulator.base.url}") String bankBaseUrl) {
    this.restTemplate = restTemplate;
    this.bankBaseUrl = bankBaseUrl;
  }
  @Override
  public Health health() {
    try {
      restTemplate.getForEntity(bankBaseUrl, String.class);
      return Health.up()
          .withDetail("acquiring bank", "reachable")
          .build();
    } catch (HttpStatusCodeException e) {
      // http 4xx or 5xx response so still suggesting the acquiring bank is reachable
      return Health.up()
          .withDetail("acquiring bank", "reachable")
          .build();
    } catch (Exception e) {
      // No response at all meaning possible connection refused or timeout
      return Health.down()
          .withDetail("acquiring bank", "unreachable")
          .build();
    }
  }
}