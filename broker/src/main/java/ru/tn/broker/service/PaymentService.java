package ru.tn.broker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.tn.broker.repository.PaymentRepository;
import ru.tn.broker.exception.PaymentNotFoundException;
import ru.tn.model.Payment;

import java.net.URI;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public ResponseEntity<?> pay(Payment payment) {
        Integer id = paymentRepository.save(payment).getId();

        if (id != null) {
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(id)
                    .toUri();

            return ResponseEntity.created(location).build();
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    public Payment getPayment(Integer id) {
        return paymentRepository.findById(id).orElseThrow(
                () -> new PaymentNotFoundException(id));
    }
}
