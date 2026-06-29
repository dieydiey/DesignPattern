package com.example.badwallet_api.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {
    private Long id;
    private String phoneNumber;
    private String email;
    private BigDecimal balance;
    private String code;
    private String currency;
}