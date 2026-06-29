package com.example.badwallet_api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    @NotNull @Positive
    private BigDecimal amount;
    private String paymentMethod; // CREDIT_CARD ou WALLET_TARGET
}
