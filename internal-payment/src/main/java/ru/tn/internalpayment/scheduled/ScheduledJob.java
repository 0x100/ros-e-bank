package ru.tn.internalpayment.scheduled;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tn.internalpayment.controller.FeignController;
import ru.tn.model.Payment;

import java.math.BigDecimal;

@Component
public class ScheduledJob {

    @Autowired
    private FeignController extPay;

    @Scheduled(fixedDelay = 3000)
    @HystrixCommand(fallbackMethod = "defaultCallExternalPayment", commandProperties = {@HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")})
    public void callExternalPayment() {
        Payment payment = new Payment();
        payment.setAccountNumber("00009999");
        payment.setClientName("Client FIO");
        payment.setTransferSum(BigDecimal.valueOf(1000));

        extPay.pay(payment);
    }

    public void defaultCallExternalPayment() {

    }
}
