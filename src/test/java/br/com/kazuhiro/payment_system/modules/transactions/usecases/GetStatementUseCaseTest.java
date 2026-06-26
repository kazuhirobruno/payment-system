package br.com.kazuhiro.payment_system.modules.transactions.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.StatementItemResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.services.ValidateUserService;
import br.com.kazuhiro.payment_system.types.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
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

@ExtendWith(MockitoExtension.class)
class GetStatementUseCaseTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private ValidateUserService validateUserService;

  @InjectMocks
  private GetStatementUseCase getStatementUseCase;

  private UUID dummyUserUuid;
  private String dummyUserId;
  private UserEntity dummyUser;
  private Pageable pageable;
  private Instant fixedTime = Instant.parse("2026-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    dummyUserUuid = UUID.randomUUID();
    dummyUserId = dummyUserUuid.toString();
    pageable = PageRequest.of(0, 10);

    dummyUser = UserEntity.builder()
        .id(dummyUserUuid)
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("200.00"))
        .active(true)
        .build();
  }

  @Test
  @DisplayName("Deve retornar o extrato paginado com sucesso mapeando a contraparte corretamente")
  void shouldReturnStatementWithSuccess() {
    UUID secondaryUserUuid = UUID.randomUUID();
    UserEntity secondaryUser = UserEntity.builder()
        .id(secondaryUserUuid)
        .name("Jane Doe")
        .build();

    TransactionEntity deposit = TransactionEntity.builder()
        .id(UUID.randomUUID())
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.00"))
        .sender(null)
        .receiver(dummyUser)
        .createdAt(fixedTime)
        .build();

    TransactionEntity transfer = TransactionEntity.builder()
        .id(UUID.randomUUID())
        .type(TransactionType.TRANSFER)
        .amount(new BigDecimal("50.00"))
        .sender(dummyUser)
        .receiver(secondaryUser)
        .createdAt(fixedTime)
        .build();

    Page<TransactionEntity> pageReturn = new PageImpl<>(List.of(deposit, transfer));

    doNothing().when(validateUserService).validateUserExists(dummyUserUuid);
    when(transactionRepository.findStatementByUserId(dummyUserUuid, pageable)).thenReturn(pageReturn);

    Page<StatementItemResponseDTO> result = getStatementUseCase.execute(dummyUserId, pageable);

    assertNotNull(result);
    assertEquals(2, result.getTotalElements());

    StatementItemResponseDTO firstItem = result.getContent().get(0);
    assertEquals("DEPOSIT", firstItem.getType());
    assertNull(firstItem.getCounterpartName());

    StatementItemResponseDTO secondItem = result.getContent().get(1);
    assertEquals("TRANSFER", secondItem.getType());
    assertEquals("Jane Doe", secondItem.getCounterpartName());

    verify(validateUserService, times(1)).validateUserExists(dummyUserUuid);
    verify(transactionRepository, times(1)).findStatementByUserId(dummyUserUuid, pageable);
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o ValidateUserService lançar UserNotFoundException")
  void shouldBubbleUpUserNotFoundException() {
    doThrow(new UserNotFoundException()).when(validateUserService).validateUserExists(dummyUserUuid);

    assertThrows(UserNotFoundException.class, () -> {
      getStatementUseCase.execute(dummyUserId, pageable);
    });

    verify(transactionRepository, never()).findStatementByUserId(any(UUID.class), any(Pageable.class));
  }

  @Test
  @DisplayName("Deve repassar a exceção quando o ValidateUserService lançar DeletedUserLoginException")
  void shouldBubbleUpDeletedUserLoginException() {
    doThrow(new DeletedUserLoginException()).when(validateUserService).validateUserExists(dummyUserUuid);

    assertThrows(DeletedUserLoginException.class, () -> {
      getStatementUseCase.execute(dummyUserId, pageable);
    });

    verify(transactionRepository, never()).findStatementByUserId(any(UUID.class), any(Pageable.class));
  }
}
