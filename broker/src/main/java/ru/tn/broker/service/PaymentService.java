package ru.tn.broker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.tn.broker.client.PaymentFeignClient;
import ru.tn.broker.exception.PaymentNotFoundException;
import ru.tn.broker.exception.PaymentTypeNotSupportedException;
import ru.tn.broker.repository.PaymentRepository;
import ru.tn.model.Payment;

import java.net.URI;

@Service
@Transactional
public class PaymentService {

    @Value("${paymentTypeDigitsCount}")
    private int paymentTypeDigitsCount;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentServicesActuator paymentServicesActuator;

    public ResponseEntity<?> pay(Payment payment) {
        String accountNumber = payment.getAccountNumber();
        String paymentType = accountNumber.substring(accountNumber.length() - paymentTypeDigitsCount);

        PaymentFeignClient client = paymentServicesActuator.getPaymentClient(paymentType);
        if(client != null) {
            ResponseEntity<Payment> result = client.pay(payment);

            if(result.getStatusCode() == HttpStatus.CREATED) {
                payment = paymentRepository.save(payment);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(payment.getId())
                        .toUri();
                return ResponseEntity.created(location).build();
            }
        } else {
            throw new PaymentTypeNotSupportedException(paymentType);
        }
        return ResponseEntity.noContent().build();
    }

    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id));
    }
}
