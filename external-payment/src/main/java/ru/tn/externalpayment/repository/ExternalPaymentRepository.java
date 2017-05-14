package ru.tn.externalpayment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import ru.tn.model.Payment;

@CrossOrigin
@RepositoryRestResource(path = "external-payments")
public interface ExternalPaymentRepository extends CrudRepository<Payment, Integer> {
}
