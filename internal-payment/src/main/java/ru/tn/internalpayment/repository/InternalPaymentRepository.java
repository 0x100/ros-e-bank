package ru.tn.internalpayment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import ru.tn.model.Payment;

/**
 * @author dsnimshchikov on 04.05.17.
 */
@RepositoryRestResource(path = "internal-payments")
public interface InternalPaymentRepository extends CrudRepository<Payment, Integer> {
}
