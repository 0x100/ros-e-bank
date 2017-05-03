package ru.tn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.tn.model.Payment;
import ru.tn.model.PaymentStatus;
import ru.tn.repository.BrokerPaymentRepository;

@Service
public class BrokerService {

    private final BrokerPaymentRepository paymentRepository;

    @Autowired
    public BrokerService(BrokerPaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment pay(Payment payment) {
        payment.setStatus(PaymentStatus.IN_PROCESS);
        return paymentRepository.save(payment);
    }

    public Payment getPayment(Integer paymentId) {
        return paymentRepository.findOne(paymentId);
    }

    public String getState(Integer paymentId) {
        Payment payment = paymentRepository.findOne(paymentId);
        PaymentStatus status = PaymentStatus.UNKNOWN;
        if (payment != null) {
            status = payment.getStatus();
        }
        return status.name();
    }
}
