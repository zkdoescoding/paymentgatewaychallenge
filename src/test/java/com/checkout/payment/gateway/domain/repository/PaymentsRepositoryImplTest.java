package com.checkout.payment.gateway.domain.repository;

import com.checkout.payment.gateway.domain.model.Payment;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentsRepositoryImplTest {

  private PaymentsRepositoryImpl repository;

  @BeforeEach
  void setUp() {
    repository = new PaymentsRepositoryImpl();
  }

  @Test
  void whenPaymentIsAdded_thenItCanBeRetrievedById() {
    UUID id = UUID.randomUUID();
    Payment payment = new Payment(id, PaymentStatus.AUTHORIZED, "8877", 12, 2099, "USD", 100);

    repository.add(payment);

    Optional<Payment> result = repository.get(id);
    assertTrue(result.isPresent());
    assertEquals(payment, result.get());
  }

  @Test
  void whenPaymentIdDoesNotExist_thenEmptyOptionalIsReturned() {
    Optional<Payment> result = repository.get(UUID.randomUUID());
    assertTrue(result.isEmpty());
  }

  @Test
  void whenMultiplePaymentsAreAdded_thenEachIsRetrievable() {
    Payment p1 = new Payment(UUID.randomUUID(), PaymentStatus.AUTHORIZED, "0001", 1, 2099, "GBP", 10);
    Payment p2 = new Payment(UUID.randomUUID(), PaymentStatus.DECLINED, "0002", 2, 2099, "EUR", 20);

    repository.add(p1);
    repository.add(p2);

    assertEquals(p1, repository.get(p1.id()).orElseThrow());
    assertEquals(p2, repository.get(p2.id()).orElseThrow());
  }
}