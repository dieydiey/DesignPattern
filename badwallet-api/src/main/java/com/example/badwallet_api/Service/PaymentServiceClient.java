package com.example.badwallet_api.Service;

import com.example.badwallet_api.dto.FactureDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8081")
public interface PaymentServiceClient {

    @GetMapping("/api/factures/{walletCode}/current")
    List<FactureDTO> getCurrentUnpaid(
            @PathVariable("walletCode") String walletCode,
            @RequestParam(value = "unite", required = false) String unite);

    @GetMapping("/api/factures/{walletCode}/periode")
    List<FactureDTO> getUnpaidBetween(
            @PathVariable("walletCode") String walletCode,
            @RequestParam("debut") String debut,
            @RequestParam("fin") String fin);

    @PostMapping("/api/factures/mark-paid")
    void markFacturesAsPaid(@RequestBody List<String> references);
}
