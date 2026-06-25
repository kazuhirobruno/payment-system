package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.modules.transactions.dtos.StatementItemResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.services.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetStatementUseCase {
  private final TransactionRepository transactionRepository;
  private final UserService userService;

  public Page<StatementItemResponseDTO> execute(String authenticatedUserId, Pageable pageable) {
    UUID userId = UUID.fromString(authenticatedUserId);
    this.userService.validateUserExists(userId);

    Page<TransactionEntity> transactionsPage = this.transactionRepository.findStatementByUserId(userId, pageable);
    return transactionsPage.map(transaction -> {
      String counterpart = null;

      if (transaction.getSender() != null && transaction.getReceiver() != null) {
        counterpart = transaction.getSender().getId().equals(userId)
            ? transaction.getReceiver().getName()
            : transaction.getSender().getName();
      }

      return StatementItemResponseDTO.builder()
          .transactionId(transaction.getId())
          .type(transaction.getType().name())
          .amount(transaction.getAmount())
          .createdAt(transaction.getCreatedAt())
          .counterpartName(counterpart)
          .build();
    });
  }
}