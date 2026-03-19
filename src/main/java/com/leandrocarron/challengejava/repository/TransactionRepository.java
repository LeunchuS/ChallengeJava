package com.leandrocarron.challengejava.repository;

import com.leandrocarron.challengejava.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


}