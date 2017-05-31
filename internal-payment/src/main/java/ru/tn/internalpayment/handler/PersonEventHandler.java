package ru.tn.internalpayment.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import ru.tn.model.Payment;

/**
 * @author dsnimshchikov on 31.05.17.
 */
@RepositoryEventHandler(Payment.class)
public class PersonEventHandler {
    private final CounterService counterService;

    @Autowired
    public PersonEventHandler(CounterService counterService) {
        this.counterService = counterService;
    }

    @HandleBeforeCreate
    public void test(Payment payment) {
        this.counterService.increment("services.payment.pay.invoked");
    }
}
