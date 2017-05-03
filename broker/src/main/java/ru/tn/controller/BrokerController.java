package ru.tn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.tn.model.Payment;
import ru.tn.service.BrokerService;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    private final BrokerService brokerService;

    @Autowired
    public BrokerController(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @PostMapping("/pay")
    public Payment pay(@RequestBody Payment payment) {
        return brokerService.pay(payment);
    }

    @RequestMapping("/payment")
    public Payment getPayment(@RequestParam("id") Integer id) {
        return brokerService.getPayment(id);
    }

    @RequestMapping("/payment/state")
    public String getState(@RequestParam("id") Integer id) {
        return brokerService.getState(id);
    }
}
