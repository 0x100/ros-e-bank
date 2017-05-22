package ru.tn.broker.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.tn.model.Payment;

public interface PaymentFeignClient {

    @RequestMapping
    ResponseEntity<Payment> pay(Payment payment);
}
