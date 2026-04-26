package com.checkout.payment.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

  private static final long REST_TIMEOUT_MS = 10_000L;

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofMillis(REST_TIMEOUT_MS))
        .setReadTimeout(Duration.ofMillis(REST_TIMEOUT_MS))
        .build();
  }

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Payment Gateway API")
            .version("v1")
            .description("Checkout.com Payment Gateway API Challenge"));
  }
}
