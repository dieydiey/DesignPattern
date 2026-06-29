package com.example.badwallet_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class PayFacturesRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String serviceName;
    @NotEmpty
    private List<String> factureReferences;
}
