package com.example.payment_service.controller;

import com.example.payment_service.dto.FactureDTO;
import com.example.payment_service.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping("/{walletCode}/current")
    public ResponseEntity<List<FactureDTO>> getCurrentUnpaid(
            @PathVariable String walletCode,
            @RequestParam(required = false) String unite) {
        return ResponseEntity.ok(factureService.getCurrentUnpaid(walletCode, unite));
    }

    @GetMapping("/{walletCode}/periode")
    public ResponseEntity<List<FactureDTO>> getUnpaidBetween(
            @PathVariable String walletCode,
            @RequestParam String debut,
            @RequestParam String fin) {
        return ResponseEntity.ok(factureService.getUnpaidBetween(walletCode, debut, fin));
    }

    @PostMapping("/mark-paid")
    public ResponseEntity<Void> markFacturesAsPaid(@RequestBody List<String> references) {
        factureService.markFacturesAsPaid(references);
        return ResponseEntity.ok().build();
    }
}