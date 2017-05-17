package ru.tn.broker.service;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.DynamicFeignClient;
import org.springframework.cloud.netflix.feign.FeignClientHelper;
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
    private static final int TYPE_DIGITS_COUNT = 4;

    @Autowired
    private ConsulClient consulClient;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ConfigurableApplicationContext context;

    private DynamicFeignClient dynamicFeignClient = new DynamicFeignClient();

    public ResponseEntity<?> pay(Payment payment) {
        routePayment(payment);

//        Integer id = paymentRepository.save(payment).getId();
        Integer id = 1111111;
//        if (id != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();

            return ResponseEntity.created(location).build();
//        } else {
//            return ResponseEntity.noContent().build();
//        }
    }

    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id));
    }

    private void routePayment(Payment payment) {
        String accountNumber = payment.getAccountNumber();
        String paymentType = accountNumber.substring(accountNumber.length() - TYPE_DIGITS_COUNT);

        com.ecwid.consul.v1.agent.model.Service paymentServiceInstance = consulClient.getAgentServices().getValue().values().stream()
                .filter(service ->
                        service.getTags().stream().anyMatch(tag ->
                                tag.equals(MessageFormat.format(ACCOUNT_TYPE_TAG_TEMPLATE, paymentType))))
                .findAny().orElseThrow(
                        () -> new PaymentTypeNotSupportedException(paymentType));

        String serviceName = paymentServiceInstance.getService();
        String serviceUrlKey = getGatewayServiceUrlKey(serviceName);
        String url = consulClient.getKVValues(serviceUrlKey).getValue().get(0).getDecodedValue();

        PaymentFeignClient client = getPaymentFeignClient(serviceName, url);
        client.pay(payment);
    }

    private PaymentFeignClient getPaymentFeignClient(String microServiceName, String microServiceUrl) {
        Class<PaymentFeignClient> feignClientClass = PaymentFeignClient.class;
        FeignClientHelper.updateFeignClientAnnotationValue(microServiceName, feignClientClass);
        return dynamicFeignClient.create(microServiceName, microServiceUrl, feignClientClass, context);
    }
}
