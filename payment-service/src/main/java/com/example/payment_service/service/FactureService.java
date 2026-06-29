package com.example.payment_service.service;

import com.example.payment_service.entity.Facture;
import com.example.payment_service.repository.FactureRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final Random random = new Random();

    @PostConstruct
    public void initFactures() {
        // Générer des factures pour les wallets WLT-0000001 à WLT-0000010
        for (int i = 1; i <= 10; i++) {
            String walletCode = String.format("WLT-%07d", i);
            generateFacturesForWallet(walletCode);
        }
    }

    private void generateFacturesForWallet(String walletCode) {
        String[] services = {"ISM", "WOYAFAL"};
        YearMonth now = YearMonth.now();

        for (int monthOffset = 0; monthOffset < 6; monthOffset++) {
            YearMonth ym = now.minusMonths(monthOffset);
            LocalDate dueDate = ym.atEndOfMonth();

            for (String service : services) {
                for (int j = 1; j <= 3; j++) {
                    Facture facture = new Facture();
                    facture.setReference("FAC-" + service + "-" + ym.getMonthValue() + "-" + j);
                    facture.setServiceName(service);
                    facture.setWalletCode(walletCode);
                    facture.setAmount(new BigDecimal(1000 + random.nextInt(20000)));
                    facture.setDueDate(dueDate);
                    // 70% des factures sont impayées
                    facture.setPaid(random.nextDouble() < 0.3);
                    if (facture.isPaid()) {
                        facture.setPaymentDate(dueDate.minusDays(random.nextInt(10)));
                    }
                    factureRepository.save(facture);
                }
            }
        }
    }
}