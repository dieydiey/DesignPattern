package com.example.badwallet_api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {
    private Long id;
    private String reference;
    private String serviceName;
    private String walletCode;
    private BigDecimal amount;
    private LocalDate dueDate;
    private boolean paid;
    private LocalDate paymentDate;
}