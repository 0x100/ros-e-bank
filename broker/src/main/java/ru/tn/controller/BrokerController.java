package ru.tn.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.tn.model.Payment;

@RestController
@RequestMapping("/broker")
public class BrokerController {

    @PostMapping("/pay")
    public String pay(@RequestBody Payment payment, Model model) {
        model.addAttribute("payment", payment);
        return "success";
    }

    @RequestMapping("/payment/state")
    public String getState(@RequestParam("id") String paymentId) {
        return "No information";
    }
}
