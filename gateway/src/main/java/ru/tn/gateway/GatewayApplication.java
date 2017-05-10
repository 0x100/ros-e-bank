package ru.tn.gateway;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties.ZuulRoute;
import org.springframework.context.annotation.Bean;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;
import static org.springframework.util.StringUtils.hasText;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {
    private static final String GATEWAY_SERVICE_KEY = "gateway/service";

    @Autowired
	private ConsulClient consulClient;

	@Bean
    @RefreshScope
	public ZuulProperties zuulProperties() {
        List<String> gwServiceKeys = consulClient.getKVKeysOnly(GATEWAY_SERVICE_KEY).getValue();
        if(CollectionUtils.isEmpty(gwServiceKeys))
            return new ZuulProperties();

        Set<String> serviceNames = gwServiceKeys.stream()
                .map(k -> k.split("/")[2])
                .collect(Collectors.toSet());

        if(serviceNames.isEmpty())
            return new ZuulProperties();

        ZuulProperties zuulProperties = new ZuulProperties();
        Map<String, ZuulRoute> routes = new HashMap<>();

        Map<String, Service> services = consulClient.getAgentServices().getValue();
        serviceNames.forEach(serviceName -> {
            Response<List<GetValue>> values = consulClient.getKVValues(GATEWAY_SERVICE_KEY + "/" + serviceName);

            String path = "", location = "";
            for (GetValue value : values.getValue()) {
                if(value.getKey().endsWith("url")) {
                    location = value.getDecodedValue();
                } else if(value.getKey().endsWith("path")) {
                    path = value.getDecodedValue();
                }
            }

            if(hasText(path) && hasText(location)) {
                for (Service service : services.values()) {  //  todo while hasn't server side balance
                    if(service.getService().equals(serviceName)) {
                        location = format("http://{0}:{1}{2}", service.getAddress(), service.getPort().toString(), location);
                    }
                }
                routes.put(serviceName, new ZuulRoute(path, location));
            }
        });

        zuulProperties.setRoutes(routes);
        return zuulProperties;
	}

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
