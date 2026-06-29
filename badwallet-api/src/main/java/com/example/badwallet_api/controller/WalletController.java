package com.example.badwallet_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.badwallet_api.Service.*;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final SeedService seedService;

    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestParam int numWallets,
                                       @RequestParam int eventsPerWallet) {
        seedService.seedData(numWallets, eventsPerWallet);
        return ResponseEntity.ok("Seeding lancé en arrière-plan pour " + numWallets + " wallets avec " + eventsPerWallet + " événements chacun.");
    }
}
