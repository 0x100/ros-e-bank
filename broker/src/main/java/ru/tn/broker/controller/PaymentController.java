package ru.tn.broker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tn.broker.service.PaymentService;
import ru.tn.model.Payment;

@RestController
@CrossOrigin
@RequestMapping("/broker/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<?> pay(@RequestBody Payment payment) {
        return paymentService.pay(payment);
    }

    @RequestMapping("{id}")
    public Payment getPayment(@PathVariable Integer id) {
        return paymentService.getPayment(id);
    }
}
