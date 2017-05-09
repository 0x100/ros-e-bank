package ru.tn.externalpayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

@SpringBootApplication
@EnableDiscoveryClient
@EnableGatewayPublishing(@GatewayService(path = "/external-payments/**", url = "/external-payments/"))
@EntityScan("ru.tn.model")
public class ExternalPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExternalPaymentApplication.class, args);
	}

}
