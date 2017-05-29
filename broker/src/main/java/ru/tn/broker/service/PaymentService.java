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
import ru.tn.model.PaymentStatus;

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

    public ResponseEntity<Payment> pay(Payment payment) {
        String accountNumber = payment.getAccountNumber();
        String paymentType = accountNumber.substring(accountNumber.length() - paymentTypeDigitsCount);

        Integer brokerPaymentId = paymentRepository.save(payment).getId();
        PaymentFeignClient client = paymentServicesActuator.getPaymentClient(paymentType);

        if(client != null) {
            ResponseEntity<Payment> result = client.pay(payment);
            payment.setId(brokerPaymentId);

            if(result.getStatusCode() == HttpStatus.CREATED) {
                savePaymentWithStatus(payment, PaymentStatus.PAID);

                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(brokerPaymentId)
                        .toUri();
                return ResponseEntity.created(location).body(payment);
            } else {
                savePaymentWithStatus(payment, PaymentStatus.ERROR);
            }
        } else {
            payment.setId(brokerPaymentId);
            savePaymentWithStatus(payment, PaymentStatus.ERROR);

            throw new PaymentTypeNotSupportedException(paymentType);
        }
        return ResponseEntity.noContent().build();
    }

    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElseThrow(() -> new PaymentNotFoundException(id));
    }

    public Iterable<Payment> getPaymentsHistory() {
        return paymentRepository.findAll();
    }

    private void savePaymentWithStatus(Payment payment, PaymentStatus status) {
        payment.setStatus(status);
        paymentRepository.save(payment);
    }
}
