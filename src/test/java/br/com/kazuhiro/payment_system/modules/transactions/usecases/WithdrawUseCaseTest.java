package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.UserService;
import br.com.kazuhiro.payment_system.types.TransactionType;

@ExtendWith(MockitoExtension.class)
class WithdrawUseCaseTest {

  @Mock
  private UserService userService;

  @Mock
  private TransactionRepository transactionRepository;

  @InjectMocks
  private WithdrawUseCase withdrawUseCase;

  private UUID dummyUserUuid;
  private String dummyUserId;
  private UserEntity dummyUser;
  private TransactionAmountRequestDTO requestDTO;

  @BeforeEach
  void setUp() {
    dummyUserUuid = UUID.randomUUID();
    dummyUserId = dummyUserUuid.toString();

    dummyUser = UserEntity.builder()
        .id(dummyUserUuid)
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("450.00"))
        .active(true)
        .build();

    requestDTO = new TransactionAmountRequestDTO(new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Deve processar o saque com sucesso, atualizar o saldo e salvar a transação")
  void shouldExecuteWithdrawWithSuccess() {
    when(userService.withdrawAmount(dummyUserUuid, requestDTO.getAmount())).thenReturn(dummyUser);

    UUID mockTransactionId = UUID.randomUUID();
    when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
      TransactionEntity entity = invocation.getArgument(0);
      entity.setId(mockTransactionId);
      return entity;
    });

    TransactionResponseDTO response = withdrawUseCase.execute(requestDTO, dummyUserId);

    assertNotNull(response);
    assertEquals(mockTransactionId, response.getTransactionId());
    assertEquals(new BigDecimal("50.00"), response.getAmount());
    assertEquals(new BigDecimal("450.00"), response.getNewBalance());
    assertEquals(TransactionType.WITHDRAW, response.getType());
    assertNotNull(response.getCreatedAt());

    verify(userService, times(1)).withdrawAmount(dummyUserUuid, requestDTO.getAmount());
    verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar UserNotFoundException")
  void shouldBubbleUpExceptionWhenUserNotFound() {
    when(userService.withdrawAmount(dummyUserUuid, requestDTO.getAmount()))
        .thenThrow(new UserNotFoundException());

    assertThrows(UserNotFoundException.class, () -> {
      withdrawUseCase.execute(requestDTO, dummyUserId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar DeletedUserLoginException")
  void shouldBubbleUpExceptionWhenUserIsInactive() {
    when(userService.withdrawAmount(dummyUserUuid, requestDTO.getAmount()))
        .thenThrow(new DeletedUserLoginException());

    assertThrows(DeletedUserLoginException.class, () -> {
      withdrawUseCase.execute(requestDTO, dummyUserId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o UserService lançar NegativeAmountException por falta de saldo")
  void shouldBubbleUpExceptionWhenBalanceIsInsufficient() {
    when(userService.withdrawAmount(dummyUserUuid, requestDTO.getAmount()))
        .thenThrow(new NegativeAmountException());

    assertThrows(NegativeAmountException.class, () -> {
      withdrawUseCase.execute(requestDTO, dummyUserId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }
}