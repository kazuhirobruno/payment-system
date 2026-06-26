package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import br.com.kazuhiro.payment_system.modules.transactions.dtos.StatementItemResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.ValidateUserService;
import br.com.kazuhiro.payment_system.types.TransactionType;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Caso de Uso - Extrato de Conta (GetStatementUseCase)")
class GetStatementUseCaseTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private ValidateUserService validateUserService;

  @InjectMocks
  private GetStatementUseCase getStatementUseCase;

  private Instant fixedTime = Instant.parse("2026-01-01T00:00:00Z");

  private TransactionEntity createBaseTransaction() {
    TransactionEntity transaction = new TransactionEntity();
    transaction.setId(UUID.randomUUID());
    transaction.setType(TransactionType.TRANSFER);
    transaction.setAmount(new BigDecimal("100.00"));
    transaction.setCreatedAt(fixedTime);
    return transaction;
  }

  @Test
  @DisplayName("1. Deve mapear o Receiver como contraparte quando o usuário autenticado for o Sender (Ternário = True)")
  void shouldMapReceiverAsCounterpartWhenUserIsSender() {
    UUID userId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    UserEntity sender = new UserEntity();
    sender.setId(userId);
    sender.setName("Meu Usuário");

    UserEntity receiver = new UserEntity();
    receiver.setId(UUID.randomUUID());
    receiver.setName("Fulano Destinatário");

    TransactionEntity transaction = createBaseTransaction();
    transaction.setSender(sender);
    transaction.setReceiver(receiver);

    Page<TransactionEntity> page = new PageImpl<>(List.of(transaction));

    when(transactionRepository.findStatementByUserId(userId, pageable)).thenReturn(page);

    Page<StatementItemResponseDTO> result = getStatementUseCase.execute(userId.toString(), pageable);

    assertNotNull(result);
    assertEquals(1, result.getContent().size());
    assertEquals("Fulano Destinatário", result.getContent().get(0).getCounterpartName());
    verify(validateUserService, times(1)).validateUserExists(userId);
  }

  @Test
  @DisplayName("2. Deve mapear o Sender como contraparte quando o usuário autenticado for o Receiver (Ternário = False)")
  void shouldMapSenderAsCounterpartWhenUserIsReceiver() {
    UUID userId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    UserEntity sender = new UserEntity();
    sender.setId(UUID.randomUUID());
    sender.setName("Beltrano Remetente");

    UserEntity receiver = new UserEntity();
    receiver.setId(userId);
    receiver.setName("Meu Usuário");

    TransactionEntity transaction = createBaseTransaction();
    transaction.setSender(sender);
    transaction.setReceiver(receiver);

    Page<TransactionEntity> page = new PageImpl<>(List.of(transaction));

    when(transactionRepository.findStatementByUserId(userId, pageable)).thenReturn(page);

    Page<StatementItemResponseDTO> result = getStatementUseCase.execute(userId.toString(), pageable);

    assertNotNull(result);
    assertEquals("Beltrano Remetente", result.getContent().get(0).getCounterpartName());
  }

  @Test
  @DisplayName("3. Deve deixar a contraparte nula se o Sender da transação for nulo (Curto-circuito do &&)")
  void shouldLeaveCounterpartNullWhenSenderIsNull() {
    UUID userId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    UserEntity receiver = new UserEntity();
    receiver.setId(userId);

    TransactionEntity transaction = createBaseTransaction();
    transaction.setSender(null);
    transaction.setReceiver(receiver);

    Page<TransactionEntity> page = new PageImpl<>(List.of(transaction));

    when(transactionRepository.findStatementByUserId(userId, pageable)).thenReturn(page);

    Page<StatementItemResponseDTO> result = getStatementUseCase.execute(userId.toString(), pageable);

    assertNotNull(result);
    assertNull(result.getContent().get(0).getCounterpartName());
  }

  @Test
  @DisplayName("4. Deve deixar a contraparte nula se o Receiver da transação for nulo (Segunda parte do && = False)")
  void shouldLeaveCounterpartNullWhenReceiverIsNull() {
    UUID userId = UUID.randomUUID();
    Pageable pageable = PageRequest.of(0, 10);

    UserEntity sender = new UserEntity();
    sender.setId(userId);

    TransactionEntity transaction = createBaseTransaction();
    transaction.setSender(sender);
    transaction.setReceiver(null);

    Page<TransactionEntity> page = new PageImpl<>(List.of(transaction));

    when(transactionRepository.findStatementByUserId(userId, pageable)).thenReturn(page);

    Page<StatementItemResponseDTO> result = getStatementUseCase.execute(userId.toString(), pageable);

    assertNotNull(result);
    assertNull(result.getContent().get(0).getCounterpartName());
  }
}