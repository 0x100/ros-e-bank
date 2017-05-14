package ru.tn.internal.controller;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.tn.model.Payment;

/**
 * @author dsnimshchikov on 11.05.17.
 */
@FeignClient("external-payment")
public interface FeignController {

    @RequestMapping(method = RequestMethod.POST, value = "/external-payments/", consumes = "application/json")
    void pay(Payment pay);
}
