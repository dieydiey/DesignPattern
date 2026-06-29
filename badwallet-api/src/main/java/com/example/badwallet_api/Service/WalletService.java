package com.example.badwallet_api.Service;

import com.example.badwallet_api.dto.DepositRequest;
import com.example.badwallet_api.dto.FactureDTO;
import com.example.badwallet_api.dto.TransferRequest;
import com.example.badwallet_api.dto.WalletDTO;
import com.example.badwallet_api.dto.WithdrawRequest;
import com.example.badwallet_api.entity.Transaction;
import com.example.badwallet_api.entity.Wallet;
import com.example.badwallet_api.exception.InsufficientBalanceException;
import com.example.badwallet_api.exception.WalletNotFoundException;
import com.example.badwallet_api.repository.WalletRepository;
import com.example.badwallet_api.Service.PaymentServiceClient;
import com.example.badwallet_api.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionService transactionService;
    private final PaymentServiceClient paymentClient; // ← champ ajouté

    // --- CRUD ---
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
        return walletRepository.findAll(pageable).map(this::toDTO);
    }

    public Wallet getWalletByPhone(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Portefeuille non trouvé pour le numéro: " + phoneNumber));
    }

    public BigDecimal getBalance(String phoneNumber) {
        return getWalletByPhone(phoneNumber).getBalance();
    }

    // --- Opérations financières ---
    @Transactional
    public Wallet deposit(Long walletId, BigDecimal amount, String paymentMethod) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Portefeuille ID " + walletId + " non trouvé"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setWallet(wallet);
        tx.setDescription("Dépôt via " + paymentMethod);
        transactionService.saveTransaction(tx);
        return wallet;
    }

    @Transactional
    public void withdraw(String phoneNumber, BigDecimal amount) {
        Wallet wallet = getWalletByPhone(phoneNumber);
        BigDecimal fees = amount.multiply(new BigDecimal("0.01")).min(new BigDecimal("5000"));
        BigDecimal total = amount.add(fees);

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant. Nécessaire: " + total + ", disponible: " + wallet.getBalance());
        }

        wallet.setBalance(wallet.getBalance().subtract(total));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.WITHDRAW);
        tx.setAmount(amount);
        tx.setFees(fees);
        tx.setWallet(wallet);
        tx.setDescription("Retrait de " + amount + " (frais: " + fees + ")");
        transactionService.saveTransaction(tx);
    }

    @Transactional
    public void transfer(String senderPhone, String receiverPhone, BigDecimal amount) {
        Wallet sender = getWalletByPhone(senderPhone);
        Wallet receiver = getWalletByPhone(receiverPhone);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant pour le transfert.");
        }

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));
        walletRepository.save(sender);
        walletRepository.save(receiver);

        Transaction txSender = new Transaction();
        txSender.setType(Transaction.TransactionType.TRANSFER);
        txSender.setAmount(amount);
        txSender.setWallet(sender);
        txSender.setReceiverPhone(receiverPhone);
        txSender.setDescription("Transfert vers " + receiverPhone);
        transactionService.saveTransaction(txSender);

        Transaction txReceiver = new Transaction();
        txReceiver.setType(Transaction.TransactionType.TRANSFER);
        txReceiver.setAmount(amount);
        txReceiver.setWallet(receiver);
        txReceiver.setReceiverPhone(senderPhone);
        txReceiver.setDescription("Transfert reçu de " + senderPhone);
        transactionService.saveTransaction(txReceiver);
    }

    // --- Paiements ---
    @Transactional
    public void payBill(String phoneNumber, String serviceName, BigDecimal amount) {
        Wallet wallet = getWalletByPhone(phoneNumber);
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant pour payer la facture.");
        }
        // Vérification simplifiée : on considère que la facture existe
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setAmount(amount);
        tx.setWallet(wallet);
        tx.setDescription("Paiement facture " + serviceName + " - " + amount);
        transactionService.saveTransaction(tx);
    }

    @Transactional
    public void payFactures(String phoneNumber, String serviceName, List<String> references) {
        Wallet wallet = getWalletByPhone(phoneNumber);
        // Récupérer les factures depuis payment-service via Feign
        List<FactureDTO> factures = paymentClient.getCurrentUnpaid(wallet.getCode(), serviceName);
        List<FactureDTO> toPay = factures.stream()
                .filter(f -> references.contains(f.getReference()))
                .toList();

        if (toPay.isEmpty()) {
            throw new com.example.badwallet_api.exception.FactureNotFoundException("Aucune facture correspondante trouvée.");
        }

        BigDecimal total = toPay.stream()
                .map(FactureDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException("Solde insuffisant pour payer les factures sélectionnées.");
        }

        wallet.setBalance(wallet.getBalance().subtract(total));
        walletRepository.save(wallet);

        // Marquer les factures comme payées dans payment-service
        paymentClient.markFacturesAsPaid(references);

        Transaction tx = new Transaction();
        tx.setType(Transaction.TransactionType.PAYMENT);
        tx.setAmount(total);
        tx.setWallet(wallet);
        tx.setDescription("Paiement de " + references.size() + " factures " + serviceName);
        transactionService.saveTransaction(tx);
    }

    public List<Transaction> getTransactionHistory(String phoneNumber) {
        Wallet wallet = getWalletByPhone(phoneNumber);
        return transactionService.getTransactionsByWallet(wallet);
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