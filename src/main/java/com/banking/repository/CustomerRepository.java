package com.banking.repository;

import com.banking.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByAccountNumber(String accountNumber);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByPan(String pan);
    Optional<Customer> findByAadhaar(String aadhaar);
    List<Customer> findByStatus(String status);
    List<Customer> findAllByOrderByCreatedAtDesc();
    boolean existsByEmail(String email);
    boolean existsByPan(String pan);
    boolean existsByAadhaar(String aadhaar);
    boolean existsByAccountNumber(String accountNumber);
}
