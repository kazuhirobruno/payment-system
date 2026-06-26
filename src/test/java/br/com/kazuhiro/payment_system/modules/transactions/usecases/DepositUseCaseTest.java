package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.BalanceService;
import br.com.kazuhiro.payment_system.types.TransactionType;

@ExtendWith(MockitoExtension.class)
class DepositUseCaseTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private BalanceService balanceService;

  @InjectMocks
  private DepositUseCase depositUseCase;

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
        .email("john@example.com")
        .balance(new BigDecimal("500.00"))
        .active(true)
        .build();

    requestDTO = new TransactionAmountRequestDTO(new BigDecimal("100.25"));
  }

  @Test
  @DisplayName("Deve processar o depósito com sucesso, atualizar saldo e salvar transação")
  void shouldExecuteDepositWithSuccess() {
    dummyUser.setBalance(new BigDecimal("600.25"));
    when(balanceService.addBalance(dummyUserUuid, requestDTO.getAmount())).thenReturn(dummyUser);

    UUID mockTransactionId = UUID.randomUUID();
    when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
      TransactionEntity entity = invocation.getArgument(0);
      entity.setId(mockTransactionId);
      return entity;
    });

    TransactionResponseDTO response = depositUseCase.execute(requestDTO, dummyUserId);

    assertNotNull(response);
    assertEquals(mockTransactionId, response.getTransactionId());
    assertEquals(new BigDecimal("100.25"), response.getAmount());
    assertEquals(new BigDecimal("600.25"), response.getNewBalance());
    assertEquals(TransactionType.DEPOSIT, response.getType());
    assertNotNull(response.getCreatedAt());

    verify(balanceService, times(1)).addBalance(dummyUserUuid, requestDTO.getAmount());
    verify(transactionRepository, times(1)).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o BalanceService lançar UserNotFoundException")
  void shouldBubbleUpExceptionWhenUserNotFound() {
    when(balanceService.addBalance(dummyUserUuid, requestDTO.getAmount()))
        .thenThrow(new UserNotFoundException());

    assertThrows(UserNotFoundException.class, () -> {
      depositUseCase.execute(requestDTO, dummyUserId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o BalanceService lançar DeletedUserLoginException")
  void shouldBubbleUpExceptionWhenUserIsInactive() {
    when(balanceService.addBalance(dummyUserUuid, requestDTO.getAmount()))
        .thenThrow(new DeletedUserLoginException());

    assertThrows(DeletedUserLoginException.class, () -> {
      depositUseCase.execute(requestDTO, dummyUserId);
    });

    verify(transactionRepository, never()).save(any(TransactionEntity.class));
  }
}