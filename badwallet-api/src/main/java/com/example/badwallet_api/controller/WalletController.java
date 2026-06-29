package com.example.badwallet_api.controller;

import com.example.badwallet_api.dto.DepositRequest;
import com.example.badwallet_api.dto.TransferRequest;
import com.example.badwallet_api.dto.WalletDTO;
import com.example.badwallet_api.dto.PayBillRequest;
import com.example.badwallet_api.dto.PayFacturesRequest;
import com.example.badwallet_api.dto.WithdrawRequest;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.Service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import com.example.badwallet_api.entity.Transaction;


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

        // 1.6 Dépôt
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Wallet> deposit(@PathVariable Long id,
                                          @RequestBody @Valid DepositRequest request) {
        return ResponseEntity.ok(walletService.deposit(id, request.getAmount(), request.getPaymentMethod()));
    }

    // 1.7 Retrait
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody @Valid WithdrawRequest request) {
        walletService.withdraw(request.getPhoneNumber(), request.getAmount());
        return ResponseEntity.ok("Retrait effectué avec succès.");
    }

    // 1.8 Transfert
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody @Valid TransferRequest request) {
        walletService.transfer(request.getSenderPhone(), request.getReceiverPhone(), request.getAmount());
        return ResponseEntity.ok("Transfert effectué avec succès.");
    }

        // 1.9 Payer une facture du mois en cours
    @PostMapping("/pay")
    public ResponseEntity<String> payBill(@RequestBody @Valid PayBillRequest request) {
        walletService.payBill(request.getPhoneNumber(), request.getServiceName(), request.getAmount());
        return ResponseEntity.ok("Facture payée avec succès.");
    }

    // 1.10 Payer des factures spécifiques
    @PostMapping("/pay-factures")
    public ResponseEntity<String> payFactures(@RequestBody @Valid PayFacturesRequest request) {
        walletService.payFactures(request.getPhoneNumber(), request.getServiceName(), request.getFactureReferences());
        return ResponseEntity.ok("Factures spécifiques payées avec succès.");
    }

    // 1.11 Consulter l'historique des transactions par téléphone
    @GetMapping("/{phoneNumber}/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable String phoneNumber) {
        return ResponseEntity.ok(walletService.getTransactionHistory(phoneNumber));
    }
}