-- ============================================================
-- SecureBank Database Schema
-- Run this in phpMyAdmin (XAMPP) before starting the app
-- Or the app auto-creates tables via Spring JPA (ddl-auto=update)
-- ============================================================

CREATE DATABASE IF NOT EXISTS banking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE banking_db;

-- The application will auto-create these tables via JPA
-- This script is provided for reference / manual setup

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    pan VARCHAR(10) NOT NULL UNIQUE,
    aadhaar VARCHAR(12) NOT NULL UNIQUE,
    account_number VARCHAR(6) UNIQUE,
    password_hash VARCHAR(255),
    balance DOUBLE NOT NULL DEFAULT 0.0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    frozen BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME,
    approved_at DATETIME,
    INDEX idx_account_number (account_number),
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_account_number VARCHAR(6) NOT NULL,
    recipient_account_number VARCHAR(6) NOT NULL,
    sender_name VARCHAR(200),
    recipient_name VARCHAR(200),
    amount DOUBLE NOT NULL,
    sender_balance_before DOUBLE DEFAULT 0,
    sender_balance_after DOUBLE DEFAULT 0,
    recipient_balance_before DOUBLE DEFAULT 0,
    recipient_balance_after DOUBLE DEFAULT 0,
    type VARCHAR(20) NOT NULL DEFAULT 'TRANSFER',
    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    description VARCHAR(255),
    created_at DATETIME,
    INDEX idx_sender (sender_account_number),
    INDEX idx_recipient (recipient_account_number)
) ENGINE=InnoDB;
