package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.services.BalanceService;
import br.com.kazuhiro.payment_system.types.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositUseCase {
  private final TransactionRepository transactionRepository;
  private final BalanceService balanceService;

  @Transactional
  public TransactionResponseDTO execute(TransactionAmountRequestDTO transactionAmountRequestDTO, String id) {
    var user = balanceService.addBalance(UUID.fromString(id), transactionAmountRequestDTO.getAmount());
    Instant now = Instant.now();
    TransactionEntity entity = TransactionEntity
        .builder()
        .amount(transactionAmountRequestDTO.getAmount())
        .receiver(user)
        .sender(null)
        .createdAt(now)
        .type(TransactionType.DEPOSIT)
        .build();

    var response = this.transactionRepository.save(entity);
    return TransactionResponseDTO.builder()
        .amount(response.getAmount())
        .createdAt(now)
        .newBalance(user.getBalance())
        .transactionId(response.getId())
        .type(response.getType())
        .build();
  }
}
