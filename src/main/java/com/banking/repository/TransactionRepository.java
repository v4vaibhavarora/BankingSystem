package com.banking.repository;

import com.banking.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.senderAccountNumber = :acct OR t.recipientAccountNumber = :acct ORDER BY t.createdAt DESC")
    List<Transaction> findByAccountNumber(@Param("acct") String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.senderAccountNumber = :acct OR t.recipientAccountNumber = :acct ORDER BY t.createdAt DESC LIMIT 5")
    List<Transaction> findTop5ByAccountNumber(@Param("acct") String accountNumber);

    List<Transaction> findAllByOrderByCreatedAtDesc();
}
