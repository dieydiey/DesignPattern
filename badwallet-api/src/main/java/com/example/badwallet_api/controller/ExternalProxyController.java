package com.example.badwallet_api.controller;

import com.example.badwallet_api.dto.FactureDTO;
import com.example.badwallet_api.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/external/factures")
@RequiredArgsConstructor
public class ExternalProxyController {

    private final PaymentServiceClient paymentClient;

    @GetMapping("/{walletCode}/current")
    public ResponseEntity<List<FactureDTO>> getCurrentUnpaid(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        return ResponseEntity.ok(paymentClient.getCurrentUnpaid(walletCode, unite));
    }

    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<FactureDTO>> getUnpaidBetween(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        return ResponseEntity.ok(paymentClient.getUnpaidBetween(walletCode, debut, fin));
    }
}