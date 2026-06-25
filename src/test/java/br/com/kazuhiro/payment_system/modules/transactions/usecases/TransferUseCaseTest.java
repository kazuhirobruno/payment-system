package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.NegativeAmountException;
import br.com.kazuhiro.payment_system.exceptions.ReceiverUserInactiveException;
import br.com.kazuhiro.payment_system.exceptions.SameAccountTransferException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.UserService;
import br.com.kazuhiro.payment_system.types.TransactionType;

@ExtendWith(MockitoExtension.class)
class TransferUseCaseTest {

  @Mock
  private UserService userService;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private TransferUseCase transferUseCase;

  private UUID senderUuid;
  private UUID receiverUuid;
  private String senderId;
  private UserEntity sender;
  private UserEntity receiver;
  private TransferRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    senderUuid = UUID.randomUUID();
    receiverUuid = UUID.randomUUID();
    senderId = senderUuid.toString();

    sender = UserEntity.builder()
        .id(senderUuid)
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("150.00"))
        .active(true)
        .build();

    receiver = UserEntity.builder()
        .id(receiverUuid)
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .active(true)
        .build();

    requestDTO = TransferRequestDTO.builder()
        .receiverId(receiverUuid)
        .amount(new BigDecimal("50.00"))
        .build();
  }

  @Test
  @DisplayName("Deve processar a transferência com sucesso, atualizar saldos e salvar o histórico")
  void shouldExecuteTransferWithSuccess() {
    sender.setBalance(new BigDecimal("100.00"));
    receiver.setBalance(new BigDecimal("150.00"));

    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenReturn(List.of(sender, receiver));

    UUID mockTransactionId = UUID.randomUUID();
    when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
      TransactionEntity entity = invocation.getArgument(0);
      entity.setId(mockTransactionId);
      return entity;
    });

    TransferResponseDTO response = transferUseCase.execute(requestDTO, senderId);

    assertNotNull(response);
    assertEquals(mockTransactionId, response.getTransactionId());
    assertEquals(TransactionType.TRANSFER.name(), response.getType());
    assertEquals(new BigDecimal("50.00"), response.getAmount());
    assertEquals(new BigDecimal("100.00"), response.getNewBalance());
    assertEquals(receiverUuid, response.getReceiverId());
    assertEquals("Jane Doe", response.getReceiverName());
    assertNotNull(response.getCreatedAt());

    verify(userService, times(1)).transferAmount(senderUuid, receiverUuid, requestDTO.getAmount());
    verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar UserNotFoundException")
  void shouldBubbleUpExceptionWhenUserNotFound() {
    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenThrow(new UserNotFoundException());

    assertThrows(UserNotFoundException.class, () -> {
      transferUseCase.execute(requestDTO, senderId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar DeletedUserLoginException")
  void shouldBubbleUpExceptionWhenSenderIsInactive() {
    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenThrow(new DeletedUserLoginException());

    assertThrows(DeletedUserLoginException.class, () -> {
      transferUseCase.execute(requestDTO, senderId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar ReceiverUserInactiveException")
  void shouldBubbleUpExceptionWhenReceiverIsInactive() {
    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenThrow(new ReceiverUserInactiveException());

    assertThrows(ReceiverUserInactiveException.class, () -> {
      transferUseCase.execute(requestDTO, senderId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar SameAccountTransferException")
  void shouldBubbleUpExceptionWhenTransferringToSelf() {
    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenThrow(new SameAccountTransferException());

    assertThrows(SameAccountTransferException.class, () -> {
      transferUseCase.execute(requestDTO, senderId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar NegativeAmountException por falta de saldo")
  void shouldBubbleUpExceptionWhenBalanceIsInsufficient() {
    when(userService.transferAmount(senderUuid, receiverUuid, requestDTO.getAmount()))
        .thenThrow(new NegativeAmountException());

    assertThrows(NegativeAmountException.class, () -> {
      transferUseCase.execute(requestDTO, senderId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }
}