package com.banking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String senderAccountNumber;

    @Column(nullable = false)
    private String recipientAccountNumber;

    private String senderName;
    private String recipientName;

    @Column(nullable = false)
    private double amount;

    private double senderBalanceBefore;
    private double senderBalanceAfter;
    private double recipientBalanceBefore;
    private double recipientBalanceAfter;

    @Column(nullable = false)
    private String type = "TRANSFER"; // TRANSFER, CREDIT, DEBIT

    @Column(nullable = false)
    private String status = "SUCCESS"; // SUCCESS, FAILED

    private String description;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    // ---- Getters & Setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String v) { this.senderAccountNumber = v; }
    public String getRecipientAccountNumber() { return recipientAccountNumber; }
    public void setRecipientAccountNumber(String v) { this.recipientAccountNumber = v; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String v) { this.senderName = v; }
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String v) { this.recipientName = v; }
    public double getAmount() { return amount; }
    public void setAmount(double v) { this.amount = v; }
    public double getSenderBalanceBefore() { return senderBalanceBefore; }
    public void setSenderBalanceBefore(double v) { this.senderBalanceBefore = v; }
    public double getSenderBalanceAfter() { return senderBalanceAfter; }
    public void setSenderBalanceAfter(double v) { this.senderBalanceAfter = v; }
    public double getRecipientBalanceBefore() { return recipientBalanceBefore; }
    public void setRecipientBalanceBefore(double v) { this.recipientBalanceBefore = v; }
    public double getRecipientBalanceAfter() { return recipientBalanceAfter; }
    public void setRecipientBalanceAfter(double v) { this.recipientBalanceAfter = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}
