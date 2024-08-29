package ar.edu.utn.frbb.tup.persistence.entity;

import java.time.LocalDateTime;

public class TransferEntity {
    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;
    private LocalDateTime transferDate;
    private String description;

    // Getters and Setters
}