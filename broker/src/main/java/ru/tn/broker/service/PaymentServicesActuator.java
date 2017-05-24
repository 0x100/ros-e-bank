package ru.tn.broker.service;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.feign.DynamicFeignClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tn.broker.client.PaymentFeignClient;
import ru.tn.broker.exception.PaymentTypeNotSupportedException;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static ru.tn.gateway.publish.config.GatewayPublisherConfiguration.getGatewayServiceUrlKey;

@Component
public class PaymentServicesActuator {

    private static final int CHECK_INTERVAL = 1000;

    @Value("${paymentTypeDigitsCount}")
    private int paymentTypeDigitsCount;

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private ConfigurableApplicationContext context;

    private Map<String, String> paymentServicesClients = new HashMap<>();
    private DynamicFeignClient dynamicFeignClient = new DynamicFeignClient();

    public PaymentFeignClient getPaymentClient(String paymentType) {
        String beanName = paymentServicesClients.get(paymentType);
        if(context.containsBean(beanName)) {
            return (PaymentFeignClient) context.getBean(beanName);
        }
        return null;
    }

    @Scheduled(fixedDelay = CHECK_INTERVAL)
    private void checkServices() {
        consulClient.getAgentServices().getValue().values().forEach(service -> {
                Optional<String> paymentType = service.getTags().stream()
                        .filter(tag ->
                                tag.matches("\\w+=\\d{" + paymentTypeDigitsCount + "}"))
                        .map(value ->
                                value.substring(value.length() - paymentTypeDigitsCount)
                        )
                        .findFirst();
                if (paymentType.isPresent() && !paymentServicesClients.containsKey(paymentType.get())) {
                    String serviceName = service.getService();
                    String serviceUrlKey = getGatewayServiceUrlKey(serviceName);
                    String url = consulClient.getKVValues(serviceUrlKey).getValue().get(0).getDecodedValue();

                    paymentServicesClients.put(paymentType.get(), getPaymentFeignClientBeanName(serviceName, url));
                }
            }
        );
    }

    private String getPaymentFeignClientBeanName(String microServiceName, String methodUrl) {
        return dynamicFeignClient.create(microServiceName, methodUrl, PaymentFeignClient.class, context);
    }
}
