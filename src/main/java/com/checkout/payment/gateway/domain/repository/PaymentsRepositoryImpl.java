package com.checkout.payment.gateway.domain.repository;

import com.checkout.payment.gateway.domain.model.Payment;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

import static org.slf4j.LoggerFactory.getLogger;

@Repository
public class PaymentsRepositoryImpl implements PaymentsRepository{
  private static final Logger LOG = getLogger(PaymentsRepositoryImpl.class);
  private final ConcurrentHashMap<UUID, Payment> payments = new ConcurrentHashMap<>();

  @Override
  public void add(Payment payment) {
    payments.put(payment.id(), payment);
    LOG.debug("Payment recorded to db with paymentId={} status={}", payment.id(), payment.status());
  }

  @Override
  public Optional<Payment> get(UUID id) {
    return Optional.ofNullable(payments.get(id));
  }

}
