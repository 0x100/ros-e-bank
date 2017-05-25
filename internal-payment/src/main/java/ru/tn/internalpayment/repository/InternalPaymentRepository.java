package ru.tn.internalpayment.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;
import ru.tn.model.Payment;

/**
 * @author dsnimshchikov on 04.05.17.
 */
@CrossOrigin
@RepositoryRestResource(path = "internal-payments")
public interface InternalPaymentRepository extends CrudRepository<Payment, Integer> {
}
