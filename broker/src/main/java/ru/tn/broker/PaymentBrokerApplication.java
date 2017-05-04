package ru.tn.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("ru.tn.model")
public class PaymentBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentBrokerApplication.class, args);
    }
}
