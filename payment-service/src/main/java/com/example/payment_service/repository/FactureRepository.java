package com.example.payment_service.repository;


import com.example.payment_service.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    List<Facture> findByWalletCodeAndPaidFalseAndDueDateBetween(
            String walletCode,
            LocalDate start,
            LocalDate end);

    List<Facture> findByWalletCodeAndPaidFalseAndDueDateBetweenAndServiceName(
            String walletCode,
            LocalDate start,
            LocalDate end,
            String serviceName);

    List<Facture> findByReferenceIn(List<String> references);
}