package ru.tn.broker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tn.broker.service.PaymentService;
import ru.tn.model.Payment;

@RestController
@CrossOrigin
@EnableCircuitBreaker
@RequestMapping("/broker/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<Payment> pay(@RequestBody Payment payment) {
        return paymentService.pay(payment);
    }

    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable Integer id) {
        return paymentService.getPayment(id);
    }

    @GetMapping
    public Iterable<Payment> getPaymentsHistory() {
        return paymentService.getPaymentsHistory();
    }
}
