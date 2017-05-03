package ru.tn.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.tn.model.Payment;

@Repository
public interface BrokerPaymentRepository extends CrudRepository<Payment, Integer> {

}
