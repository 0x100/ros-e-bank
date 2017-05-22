package ru.tn.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Table
@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String clientName;
    @NotNull
    private String accountNumber;

    @NotNull
    private BigDecimal transferSum;
    private PaymentStatus status;
}
