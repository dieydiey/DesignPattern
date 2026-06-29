package com.example.badwallet_api.Service;

import com.example.badwallet_api.dto.WalletDTO;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.exception.WalletNotFoundException;
import com.example.badwallet_api.repository.WalletRepository;
import com.example.badwallet_api.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    @Transactional
    public Wallet createWallet(String phoneNumber, String email, BigDecimal initialBalance, String currency) {
        if (walletRepository.existsByPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Ce numéro de téléphone est déjà utilisé.");
        }
        Wallet wallet = new Wallet();
        wallet.setPhoneNumber(phoneNumber);
        wallet.setEmail(email);
        wallet.setBalance(initialBalance != null ? initialBalance : BigDecimal.ZERO);
        wallet.setCode(CodeGenerator.generateWalletCode());
        wallet.setCurrency(currency != null ? currency : "XOF");
        return walletRepository.save(wallet);
    }

    public Page<WalletDTO> getAllWallets(Pageable pageable) {
        return walletRepository.findAll(pageable)
                .map(this::toDTO);
    }

    public Wallet getWalletByPhone(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Portefeuille non trouvé pour le numéro: " + phoneNumber));
    }

    public BigDecimal getBalance(String phoneNumber) {
        return getWalletByPhone(phoneNumber).getBalance();
    }

    private WalletDTO toDTO(Wallet wallet) {
        return new WalletDTO(
                wallet.getId(),
                wallet.getPhoneNumber(),
                wallet.getEmail(),
                wallet.getBalance(),
                wallet.getCode(),
                wallet.getCurrency()
        );
    }
}
