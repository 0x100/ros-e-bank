package ru.tn.broker.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tn.model.Payment;

@FeignClient("template")
public interface PaymentFeignClient {

    @RequestMapping
    ResponseEntity<Payment> pay(Payment payment);
}
