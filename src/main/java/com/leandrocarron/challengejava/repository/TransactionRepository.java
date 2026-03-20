package com.leandrocarron.challengejava.repository;

import com.leandrocarron.challengejava.dto.responseDTO.Ranked;
import com.leandrocarron.challengejava.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

      @Query("SELECT COALESCE(SUM(t.amount),0) FROM Transaction t WHERE t.accountId = :accountId")
      BigDecimal sumAmountByAccountId(@Param("accountId") Long accountId);

      @Query("SELECT t.accountId as accountId, count(t.transactionId) as processed FROM Transaction t GROUP BY t.accountId ORDER BY count(t.transactionId) DESC")
      List<Ranked> getRanking(Pageable pageable);

}