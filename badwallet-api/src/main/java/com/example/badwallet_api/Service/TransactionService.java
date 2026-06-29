package com.example.badwallet_api.Service;

import com.example.badwallet_api.entity.Transaction;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionsByWallet(Wallet wallet) {
        return transactionRepository.findByWalletOrderByDateDesc(wallet);
    }
}