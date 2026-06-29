package com.example.badwallet_api.controller;

import com.example.badwallet_api.dto.WalletDTO;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final SeedService seedService;

    // 1.1 Seeder
    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam int numWallets,
                                       @RequestParam int eventsPerWallet) {
        seedService.seedData(numWallets, eventsPerWallet);
        return ResponseEntity.ok("Seeding lancé en arrière-plan pour " + numWallets + " wallets avec " + eventsPerWallet + " événements chacun.");
    }

    // 1.2 Créer un wallet
    @PostMapping
    public ResponseEntity<Wallet> createWallet(@RequestBody @Valid WalletDTO dto) {
        Wallet wallet = walletService.createWallet(
                dto.getPhoneNumber(),
                dto.getEmail(),
                dto.getBalance(),
                dto.getCurrency()
        );
        return ResponseEntity.ok(wallet);
    }

    // 1.3 Lister tous les wallets (paginé)
    @GetMapping
    public ResponseEntity<Page<WalletDTO>> listWallets(Pageable pageable) {
        return ResponseEntity.ok(walletService.getAllWallets(pageable));
    }

    // 1.4 Consulter un wallet par téléphone
    @GetMapping("/{phoneNumber}")
    public ResponseEntity<Wallet> getWallet(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.getWalletByPhone(phoneNumber));
    }

    // 1.5 Consulter le solde
    @GetMapping("/{phoneNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.getBalance(phoneNumber));
    }
}