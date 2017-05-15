package ru.tn.broker.client;

import org.springframework.http.ResponseEntity;
import ru.tn.model.Payment;

public interface PaymentFeignClient {

    ResponseEntity<Payment> pay(Payment payment);
}
