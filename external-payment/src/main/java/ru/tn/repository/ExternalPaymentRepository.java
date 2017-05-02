package ru.tn.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.tn.model.Payment;

@RepositoryRestResource(path = "external-payments")
public interface ExternalPaymentRepository extends CrudRepository<Payment, Integer> {
}
