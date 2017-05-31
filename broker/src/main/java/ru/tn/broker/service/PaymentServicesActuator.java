package ru.tn.broker.service;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.feign.DynamicFeignClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.tn.broker.client.PaymentFeignClient;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.tn.gateway.publish.config.GatewayPublisherConfiguration.getGatewayServiceUrlKey;

@Component
@RefreshScope
@Profile("!test")
public class PaymentServicesActuator {

    @Value("${paymentTypeDigitsCount}")
    private int paymentTypeDigitsCount;

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private ConfigurableApplicationContext context;

    private Map<String, PaymentFeignClient> paymentServicesClients = new HashMap<>();

    public PaymentFeignClient getPaymentClient(String paymentType) {
        return paymentServicesClients.get(paymentType);
    }

    @PostConstruct
    public void checkServices() {
        consulClient.getAgentServices().getValue().values().forEach(service -> {
                Optional<String> paymentType = service.getTags().stream()
                        .filter(tag ->
                                tag.matches("\\w+=\\d{" + paymentTypeDigitsCount + "}"))
                        .map(value ->
                                value.substring(value.length() - paymentTypeDigitsCount))
                        .findFirst();
                if (paymentType.isPresent() && !paymentServicesClients.containsKey(paymentType.get())) {
                    String serviceName = service.getService();
                    String serviceUrlKey = getGatewayServiceUrlKey(serviceName);
                    String url = consulClient.getKVValues(serviceUrlKey).getValue().get(0).getDecodedValue();

                    PaymentFeignClient client = generatePaymentFeignClient(serviceName, url);
                    paymentServicesClients.put(paymentType.get(), client);
                }
            }
        );
    }

    private PaymentFeignClient generatePaymentFeignClient(String microServiceName, String methodUrl) {
        DynamicFeignClient clientBuilder = new DynamicFeignClient();
        return clientBuilder.create(microServiceName, methodUrl, PaymentFeignClient.class, context);
    }
}
