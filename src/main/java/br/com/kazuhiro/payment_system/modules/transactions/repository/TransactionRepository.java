package br.com.kazuhiro.payment_system.modules.transactions.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;

import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
  @Query("SELECT t FROM TransactionEntity t WHERE t.sender.id = :userId OR t.receiver.id = :userId ORDER BY t.createdAt DESC")
  Page<TransactionEntity> findStatementByUserId(UUID userId, Pageable pageable);
}
