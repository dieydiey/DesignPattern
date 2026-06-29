package com.example.badwallet_api.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.badwallet_api.entity.Transaction;
import com.example.badwallet_api.entity.Wallet;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletOrderByDateDesc(Wallet wallet);
}
