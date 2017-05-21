package ru.tn.internalpayment.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.tn.internalpayment.controller.FeignController;
import ru.tn.model.Payment;
import ru.tn.model.PaymentStatus;

import java.math.BigDecimal;

/**
 * @author dsnimshchikov on 13.05.17.
 */
@Component
public class ScheduledJob {
    @Autowired
    FeignController extPay;
    @Autowired
    private DiscoveryClient discoveryClient;

    @Scheduled(fixedDelay = 3000)
    public void callExternalPayment() {

        Payment payment = new Payment();
        payment.setAccountNumber("00009999");
        payment.setClientName("Client FIO");
        payment.setStatus(PaymentStatus.UNKNOWN);
        payment.setTransferSum(BigDecimal.valueOf(1000));

        extPay.pay(payment);
    }
}
