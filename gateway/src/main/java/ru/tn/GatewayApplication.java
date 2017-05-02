package ru.tn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
    @RefreshScope
	public ZuulProperties zuulProperties() {
		ZuulProperties zuulProperties = new ZuulProperties();
		Map<String, ZuulRoute> routes = new HashMap<>();
        String text = "/tests/**=/tests/v1";
        routes.put("test", new ZuulRoute(text));
		zuulProperties.setRoutes(routes);
		return zuulProperties;
	}

}
