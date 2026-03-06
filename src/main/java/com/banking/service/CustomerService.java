package com.banking.service;

import com.banking.model.Customer;
import com.banking.model.Transaction;
import com.banking.repository.CustomerRepository;
import com.banking.repository.TransactionRepository;
import com.banking.util.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomerService {

    @Autowired private CustomerRepository customerRepo;
    @Autowired private TransactionRepository txRepo;
    @Autowired private AccountUtils accountUtils;
    @Autowired private EmailService emailService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // ─── KYC Submission ───────────────────────────────────────────────
    @Transactional
    public Customer submitKyc(String fullName, String email, String pan, String aadhaar) {
        if (customerRepo.existsByEmail(email)) throw new RuntimeException("Email already registered");
        if (customerRepo.existsByPan(pan.toUpperCase())) throw new RuntimeException("PAN already registered");
        if (customerRepo.existsByAadhaar(aadhaar)) throw new RuntimeException("Aadhaar already registered");

        Customer c = new Customer();
        c.setFullName(fullName.trim());
        c.setEmail(email.trim().toLowerCase());
        c.setPan(pan.trim().toUpperCase());
        c.setAadhaar(aadhaar.trim());
        c.setStatus("PENDING");
        c.setBalance(0.0);
        c.setFrozen(false);
        return customerRepo.save(c);
    }

    // ─── Approve KYC ──────────────────────────────────────────────────
    @Transactional
    public Map<String, String> approveKyc(Long customerId) {
        Customer c = customerRepo.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (!"PENDING".equals(c.getStatus()))
            throw new RuntimeException("Customer is not in PENDING state");

        // Generate unique 6-digit account number
        String acctNum;
        int tries = 0;
        do {
            acctNum = accountUtils.generateAccountNumber();
            tries++;
            if (tries > 100) throw new RuntimeException("Could not generate unique account number");
        } while (customerRepo.existsByAccountNumber(acctNum));

        String tempPwd = accountUtils.generateTempPassword();
        c.setAccountNumber(acctNum);
        c.setPasswordHash(encoder.encode(tempPwd));
        c.setStatus("APPROVED");
        c.setBalance(0.0);
        c.setApprovedAt(LocalDateTime.now());
        customerRepo.save(c);

        // Send email async
        emailService.sendApprovalEmail(c.getEmail(), c.getFullName(), acctNum, tempPwd);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("accountNumber", acctNum);
        result.put("tempPassword", tempPwd);
        result.put("customerName", c.getFullName());
        result.put("email", c.getEmail());
        return result;
    }

    // ─── Customer Login ───────────────────────────────────────────────
    public Customer authenticate(String accountNumber, String password) {
        Customer c = customerRepo.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new RuntimeException("Account not found. Check your account number."));
        if ("PENDING".equals(c.getStatus()))
            throw new RuntimeException("Your KYC is still pending approval.");
        if ("REJECTED".equals(c.getStatus()))
            throw new RuntimeException("Your account has been rejected. Contact the bank.");
        if (c.isFrozen() || "FROZEN".equals(c.getStatus()))
            throw new RuntimeException("Your account is frozen. Please contact the bank.");
        if (!encoder.matches(password, c.getPasswordHash()))
            throw new RuntimeException("Invalid password. Please try again.");
        return c;
    }

    // ─── Money Transfer (Atomic via @Transactional) ───────────────────
    @Transactional
    public Transaction transfer(String senderAcct, String recipientAcct, double amount, String description) {
        if (amount <= 0) throw new RuntimeException("Amount must be greater than zero");
        if (senderAcct.equals(recipientAcct)) throw new RuntimeException("Cannot transfer to your own account");
        if (amount > 1_000_000) throw new RuntimeException("Transfer limit exceeded (max ₹10,00,000)");

        Customer sender = customerRepo.findByAccountNumber(senderAcct)
            .orElseThrow(() -> new RuntimeException("Sender account not found"));
        Customer recipient = customerRepo.findByAccountNumber(recipientAcct)
            .orElseThrow(() -> new RuntimeException("Recipient account number not found"));

        if (sender.isFrozen()) throw new RuntimeException("Your account is frozen");
        if (recipient.isFrozen()) throw new RuntimeException("Recipient account is currently frozen");
        if (sender.getBalance() < amount)
            throw new RuntimeException("Insufficient balance. Available: ₹" + String.format("%.2f", sender.getBalance()));

        double senderBefore = sender.getBalance();
        double recipientBefore = recipient.getBalance();
        double senderAfter = senderBefore - amount;
        double recipientAfter = recipientBefore + amount;

        sender.setBalance(senderAfter);
        recipient.setBalance(recipientAfter);
        customerRepo.save(sender);
        customerRepo.save(recipient);

        Transaction tx = new Transaction();
        tx.setSenderAccountNumber(senderAcct);
        tx.setRecipientAccountNumber(recipientAcct);
        tx.setSenderName(sender.getFullName());
        tx.setRecipientName(recipient.getFullName());
        tx.setAmount(amount);
        tx.setSenderBalanceBefore(senderBefore);
        tx.setSenderBalanceAfter(senderAfter);
        tx.setRecipientBalanceBefore(recipientBefore);
        tx.setRecipientBalanceAfter(recipientAfter);
        tx.setType("TRANSFER");
        tx.setStatus("SUCCESS");
        tx.setDescription(description != null && !description.isBlank() ? description : "Fund Transfer");
        return txRepo.save(tx);
    }

    // ─── Freeze / Unfreeze ────────────────────────────────────────────
    @Transactional
    public Customer toggleFreeze(Long customerId) {
        Customer c = customerRepo.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (!"APPROVED".equals(c.getStatus()) && !"FROZEN".equals(c.getStatus()))
            throw new RuntimeException("Can only freeze/unfreeze approved accounts");

        boolean newFrozen = !c.isFrozen();
        c.setFrozen(newFrozen);
        c.setStatus(newFrozen ? "FROZEN" : "APPROVED");
        Customer saved = customerRepo.save(c);
        emailService.sendFreezeEmail(c.getEmail(), c.getFullName(), newFrozen);
        return saved;
    }

    // ─── Helpers ──────────────────────────────────────────────────────
    public List<Customer> getAllCustomers() { return customerRepo.findAllByOrderByCreatedAtDesc(); }
    public List<Customer> getPendingKyc() { return customerRepo.findByStatus("PENDING"); }
    public Optional<Customer> findByAccountNumber(String acct) { return customerRepo.findByAccountNumber(acct); }
    public Optional<Customer> findById(Long id) { return customerRepo.findById(id); }
    public List<Transaction> getTransactions(String acct) { return txRepo.findByAccountNumber(acct); }
    public List<Transaction> getLast5(String acct) { return txRepo.findTop5ByAccountNumber(acct); }
    public List<Transaction> getAllTransactions() { return txRepo.findAllByOrderByCreatedAtDesc(); }
}
