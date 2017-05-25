package ru.tn.broker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tn.broker.service.PaymentService;
import ru.tn.model.Payment;


@CrossOrigin
@RestController
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
