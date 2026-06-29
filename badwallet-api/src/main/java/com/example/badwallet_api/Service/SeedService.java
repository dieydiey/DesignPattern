package com.example.badwallet_api.Service;


import com.example.badwallet_api.entity.Transaction;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.repository.WalletRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final Random random = new Random();
    private static int counter = 1;

    private synchronized String generateWalletCode() {
        return String.format("WLT-%07d", counter++);
    }

    @Async
    @Transactional
    public void seedData(int numWallets, int eventsPerWallet) {
        List<Wallet> wallets = new ArrayList<>();

        for (int i = 1; i <= numWallets; i++) {
            String phone = "+22177" + String.format("%07d", i);
            String code = generateWalletCode();
            Wallet wallet = new Wallet();
            wallet.setPhoneNumber(phone);
            wallet.setEmail("user" + i + "@example.com");
            wallet.setBalance(new BigDecimal("100000"));
            wallet.setCode(code);
            wallet.setCurrency("XOF");
            walletRepository.save(wallet);
            wallets.add(wallet);
        }

        // Générer des transactions pour chaque wallet
        for (Wallet wallet : wallets) {
            for (int j = 0; j < eventsPerWallet; j++) {
                generateRandomTransaction(wallet);
            }
        }
    }

    private void generateRandomTransaction(Wallet wallet) {
        int type = random.nextInt(4);
        BigDecimal amount = new BigDecimal(100 + random.nextInt(50000));
        Transaction.TransactionType txType;

        switch (type) {
            case 0 -> {
                txType = Transaction.TransactionType.DEPOSIT;
                wallet.setBalance(wallet.getBalance().add(amount));
            }
            case 1 -> {
                txType = Transaction.TransactionType.WITHDRAW;
                BigDecimal fees = amount.multiply(new BigDecimal("0.01")).min(new BigDecimal("5000"));
                if (wallet.getBalance().compareTo(amount.add(fees)) >= 0) {
                    wallet.setBalance(wallet.getBalance().subtract(amount).subtract(fees));
                } else {
                    return;
                }
            }
            case 2 -> {
                txType = Transaction.TransactionType.TRANSFER;
                if (wallet.getBalance().compareTo(amount) >= 0) {
                    wallet.setBalance(wallet.getBalance().subtract(amount));
                } else {
                    return;
                }
            }
            default -> {
                txType = Transaction.TransactionType.PAYMENT;
                if (wallet.getBalance().compareTo(amount) >= 0) {
                    wallet.setBalance(wallet.getBalance().subtract(amount));
                } else {
                    return;
                }
            }
        }

        Transaction tx = new Transaction();
        tx.setType(txType);
        tx.setAmount(amount);
        tx.setWallet(wallet);
        tx.setDate(LocalDateTime.now().minusDays(random.nextInt(30)));
        tx.setDescription("Seed transaction " + txType);
        if (txType == Transaction.TransactionType.WITHDRAW) {
            tx.setFees(amount.multiply(new BigDecimal("0.01")).min(new BigDecimal("5000")));
        }
        transactionService.saveTransaction(tx);
        walletRepository.save(wallet);
    }
}
