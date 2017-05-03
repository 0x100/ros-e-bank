package ru.tn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.tn.model.Payment;
import ru.tn.model.PaymentStatus;
import ru.tn.repository.BrokerPaymentRepository;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    private final BrokerPaymentRepository paymentRepository;

    @Autowired
    public BrokerController(BrokerPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @PostMapping("/pay")
    public Payment pay(@RequestBody Payment payment) {
        payment.setStatus(PaymentStatus.IN_PROCESS);
        return paymentRepository.save(payment);
    }

    @RequestMapping("/payment")
    public Payment getPayment(@RequestParam("id") Integer paymentId) {
        return paymentRepository.findOne(paymentId);
    }

    @RequestMapping("/payment/state")
    public PaymentStatus getState(@RequestParam("id") Integer paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        return payment != null ? payment.getStatus() : PaymentStatus.UNKNOWN;
    }
}
