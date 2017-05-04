package ru.tn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.tn.gateway.publish.annotation.EnableGatewayPublishing;
import ru.tn.gateway.publish.annotation.GatewayService;

@SpringBootApplication
@EnableGatewayPublishing(@GatewayService(path = "/external-payments/**", url = "/external-payments/"))
public class ExternalPaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExternalPaymentApplication.class, args);
	}

}
