package ru.tn.broker;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = PaymentBrokerApplication.class)
public class PaymentBrokerApplicationTest {

    @Test
    public void contextLoadTest() throws Exception {

    }
}