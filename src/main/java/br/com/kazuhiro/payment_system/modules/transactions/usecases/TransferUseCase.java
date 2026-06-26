package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.TransferAmountService;
import br.com.kazuhiro.payment_system.types.TransactionType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferUseCase {

  private final TransferAmountService transferAmountService;
  private final TransactionRepository transactionRepository;

  @Transactional
  public TransferResponseDTO execute(TransferRequestDTO transferRequestDTO, String id) {
    UUID senderId = UUID.fromString(id);
    UUID receiverId = transferRequestDTO.getReceiverId();

    List<UserEntity> involvedUsers = this.transferAmountService.transferAmount(senderId, receiverId,
        transferRequestDTO.getAmount());

    UserEntity sender = involvedUsers.stream().filter(u -> u.getId().equals(senderId)).findFirst().get();
    UserEntity receiver = involvedUsers.stream().filter(u -> u.getId().equals(receiverId)).findFirst().get();

    Instant now = Instant.now();
    TransactionEntity entity = TransactionEntity.builder()
        .amount(transferRequestDTO.getAmount())
        .sender(sender)
        .receiver(receiver)
        .createdAt(now)
        .type(TransactionType.TRANSFER)
        .build();

    var response = this.transactionRepository.save(entity);

    return TransferResponseDTO.builder()
        .transactionId(response.getId())
        .type(response.getType().name())
        .amount(response.getAmount())
        .newBalance(sender.getBalance())
        .receiverId(receiver.getId())
        .receiverName(receiver.getName())
        .createdAt(now)
        .build();
  }
}