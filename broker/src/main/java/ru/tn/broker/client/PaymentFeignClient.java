package ru.tn.broker.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.tn.model.Payment;

@FeignClient("template")
public interface PaymentFeignClient {

    @RequestMapping(value = "template", method = RequestMethod.POST)
    ResponseEntity<Payment> pay(Payment payment);
}
