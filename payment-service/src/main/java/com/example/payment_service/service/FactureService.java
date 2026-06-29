package com.example.payment_service.service;

import com.example.payment_service.dto.FactureDTO;
import com.example.payment_service.entity.Facture;
import com.example.payment_service.repository.FactureRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;
    private final Random random = new Random();

    @PostConstruct
    public void initFactures() {
        // Générer des factures pour les codes WLT-0000001 à WLT-0000010
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
                    // Référence unique : on inclut le walletCode pour éviter les doublons
                    String reference = "FAC-" + service + "-" + walletCode + "-" + ym.getMonthValue() + "-" + j;
                    Facture facture = new Facture();
                    facture.setReference(reference);
                    facture.setServiceName(service);
                    facture.setWalletCode(walletCode);
                    facture.setAmount(new BigDecimal(1000 + random.nextInt(20000)));
                    facture.setDueDate(dueDate);
                    // 70% des factures impayées
                    facture.setPaid(random.nextDouble() < 0.3);
                    if (facture.isPaid()) {
                        facture.setPaymentDate(dueDate.minusDays(random.nextInt(10)));
                    }
                    factureRepository.save(facture);
                }
            }
        }
    }

    public List<FactureDTO> getCurrentUnpaid(String walletCode, String serviceName) {
        YearMonth now = YearMonth.now();
        LocalDate start = now.atDay(1);
        LocalDate end = now.atEndOfMonth();

        List<Facture> factures;
        if (serviceName != null && !serviceName.isEmpty()) {
            factures = factureRepository.findByWalletCodeAndPaidFalseAndDueDateBetweenAndServiceName(
                    walletCode, start, end, serviceName);
        } else {
            factures = factureRepository.findByWalletCodeAndPaidFalseAndDueDateBetween(
                    walletCode, start, end);
        }
        return factures.stream().map(this::toDTO).toList();
    }

    public List<FactureDTO> getUnpaidBetween(String walletCode, String debut, String fin) {
        LocalDate start = LocalDate.parse(debut);
        LocalDate end = LocalDate.parse(fin);
        List<Facture> factures = factureRepository.findByWalletCodeAndPaidFalseAndDueDateBetween(
                walletCode, start, end);
        return factures.stream().map(this::toDTO).toList();
    }

    @Transactional
    public void markFacturesAsPaid(List<String> references) {
        List<Facture> factures = factureRepository.findByReferenceIn(references);
        for (Facture f : factures) {
            f.setPaid(true);
            f.setPaymentDate(LocalDate.now());
        }
        factureRepository.saveAll(factures);
    }

    private FactureDTO toDTO(Facture f) {
        return new FactureDTO(
                f.getId(),
                f.getReference(),
                f.getServiceName(),
                f.getWalletCode(),
                f.getAmount(),
                f.getDueDate(),
                f.isPaid(),
                f.getPaymentDate()
        );
    }
}