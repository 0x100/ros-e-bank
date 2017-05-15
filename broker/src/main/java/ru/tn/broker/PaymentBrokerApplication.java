package ru.tn.broker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableGatewayPublishing(@GatewayService(path = "/broker/**", url = "/broker/"))
@EntityScan("ru.tn.model")
public class PaymentBrokerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentBrokerApplication.class, args);
    }
}
