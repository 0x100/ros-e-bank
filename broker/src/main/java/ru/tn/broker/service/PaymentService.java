package ru.tn.broker.service;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClientRegistrar;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.tn.broker.client.PaymentFeignClient;
import ru.tn.broker.exception.PaymentNotFoundException;
import ru.tn.broker.exception.PaymentTypeNotSupportedException;
import ru.tn.broker.repository.PaymentRepository;
import ru.tn.model.Payment;

import java.net.URI;
import java.text.MessageFormat;

import static ru.tn.gateway.publish.config.GatewayPublisherConfiguration.getGatewayServiceUrlKey;

@Service
@Transactional
public class PaymentService {

    private static final String ACCOUNT_TYPE_TAG_TEMPLATE = "accountNumber={0}";

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ConfigurableApplicationContext context;

    private FeignClientRegistrar feignClientRegistrar = new FeignClientRegistrar();

    public ResponseEntity<?> pay(Payment payment) {
        Integer id = paymentRepository.save(payment).getId();

        if (id != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();

            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    public Payment getPayment(Integer id) {
        testRouting();
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id));
    }

    private void testRouting() {
        String accountNumber = "111110000";
        String paymentType = accountNumber.substring(accountNumber.length() - 4);

        com.ecwid.consul.v1.agent.model.Service serviceInstance = consulClient.getAgentServices().getValue().values().stream().filter(service ->
                service.getTags().stream().anyMatch(tag -> tag.equals(MessageFormat.format(ACCOUNT_TYPE_TAG_TEMPLATE, paymentType))))
                .findAny().orElseThrow(
                        () -> new PaymentTypeNotSupportedException(paymentType));

        String serviceName = serviceInstance.getService();
        String serviceUrlKey = getGatewayServiceUrlKey(serviceName);
        String path = consulClient.getKVValues(serviceUrlKey).getValue().get(0).getDecodedValue();

        System.out.println("serviceName = " + serviceName);
        System.out.println("path = " + path);
        String url = "";

        Payment payment = new Payment();
        payment.setAccountNumber(accountNumber);

        PaymentFeignClient client = getPaymentFeignClient(serviceName, path, url);
        client.pay(payment);
    }

    private PaymentFeignClient getPaymentFeignClient(String serviceName, String path, String url) {
        return feignClientRegistrar.create(serviceName, url, path, PaymentFeignClient.class, context);
    }
}
