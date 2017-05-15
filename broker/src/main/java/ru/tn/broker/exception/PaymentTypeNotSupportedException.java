package ru.tn.broker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class PaymentTypeNotSupportedException extends RuntimeException {

    public PaymentTypeNotSupportedException(String paymentType) {
        super(MessageFormat.format("Payment type {0} not supported", paymentType));
    }
}
