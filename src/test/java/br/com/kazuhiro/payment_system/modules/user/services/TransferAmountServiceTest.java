package br.com.kazuhiro.payment_system.modules.user.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransferAmountServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TransferAmountService transferAmountService;

  private UUID dummyUserId;
  private UserEntity activeUser;
  private UserEntity inactiveUser;

  @BeforeEach
  void setUp() {
    dummyUserId = UUID.randomUUID();

    activeUser = UserEntity.builder()
        .id(dummyUserId)
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("200.00"))
        .active(true)
        .build();

    inactiveUser = UserEntity.builder()
        .id(dummyUserId)
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("200.00"))
        .active(false)
        .build();
  }

  @Test
  @DisplayName("Deve transferir o valor entre contas com sucesso quando ambos estiverem ativos e houver saldo")
  void shouldTransferAmountWithSuccess() {
    UUID receiverId = UUID.randomUUID();
    UserEntity receiverUser = UserEntity.builder()
        .id(receiverId)
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .active(true)
        .build();

    BigDecimal transferAmount = new BigDecimal("50.00");

    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;
    UUID secondLockId = firstLockId.equals(dummyUserId) ? receiverId : dummyUserId;

    UserEntity firstMockReturn = firstLockId.equals(dummyUserId) ? activeUser : receiverUser;
    UserEntity secondMockReturn = secondLockId.equals(dummyUserId) ? activeUser : receiverUser;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.of(firstMockReturn));
    when(userRepository.findByIdForUpdate(secondLockId)).thenReturn(Optional.of(secondMockReturn));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    List<UserEntity> result = transferAmountService.transferAmount(dummyUserId, receiverId, transferAmount);

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(new BigDecimal("150.00"), activeUser.getBalance());
    assertEquals(new BigDecimal("150.00"), receiverUser.getBalance());
    verify(userRepository, times(1)).findByIdForUpdate(firstLockId);
    verify(userRepository, times(1)).findByIdForUpdate(secondLockId);
    verify(userRepository, times(2)).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar SameAccountTransferException ao tentar transferir para a própria conta")
  void shouldThrowSameAccountTransferExceptionWhenTransferringToSelf() {
    BigDecimal amount = BigDecimal.valueOf(10.00);
    assertThrows(SameAccountTransferException.class,
        () -> transferAmountService.transferAmount(dummyUserId, dummyUserId, amount));

    verify(userRepository, never()).findByIdForUpdate(any(UUID.class));
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserNotFoundException quando o primeiro ID da busca com lock não existir")
  void shouldThrowUserNotFoundExceptionWhenFirstUserDoesNotExist() {
    UUID receiverId = UUID.randomUUID();
    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.empty());
    BigDecimal amount = BigDecimal.valueOf(10.00);
    assertThrows(UserNotFoundException.class,
        () -> transferAmountService.transferAmount(dummyUserId, receiverId, amount));

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserNotFoundException quando o segundo ID da busca com lock não existir")
  void shouldThrowUserNotFoundExceptionWhenSecondUserDoesNotExist() {
    UUID receiverId = UUID.randomUUID();
    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;
    UUID secondLockId = firstLockId.equals(dummyUserId) ? receiverId : dummyUserId;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.of(activeUser));
    when(userRepository.findByIdForUpdate(secondLockId)).thenReturn(Optional.empty());
    BigDecimal amount = BigDecimal.valueOf(10.00);
    assertThrows(UserNotFoundException.class,
        () -> transferAmountService.transferAmount(dummyUserId, receiverId, amount));

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar DeletedUserLoginException quando o remetente estiver inativo")
  void shouldThrowDeletedUserLoginExceptionWhenSenderIsInactive() {
    UUID receiverId = UUID.randomUUID();
    UserEntity receiverUser = UserEntity.builder()
        .id(receiverId)
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .active(true)
        .build();

    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;
    UUID secondLockId = firstLockId.equals(dummyUserId) ? receiverId : dummyUserId;

    UserEntity firstMockReturn = firstLockId.equals(dummyUserId) ? inactiveUser : receiverUser;
    UserEntity secondMockReturn = secondLockId.equals(dummyUserId) ? inactiveUser : receiverUser;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.of(firstMockReturn));
    when(userRepository.findByIdForUpdate(secondLockId)).thenReturn(Optional.of(secondMockReturn));

    BigDecimal amount = BigDecimal.valueOf(10.00);
    assertThrows(DeletedUserLoginException.class,
        () -> transferAmountService.transferAmount(dummyUserId, receiverId, amount));

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar ReceiverUserInactiveException quando o destinatário estiver inativo")
  void shouldThrowReceiverUserInactiveExceptionWhenReceiverIsInactive() {
    UUID receiverId = UUID.randomUUID();
    UserEntity inactiveReceiver = UserEntity.builder()
        .id(receiverId)
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .active(false)
        .build();

    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;
    UUID secondLockId = firstLockId.equals(dummyUserId) ? receiverId : dummyUserId;

    UserEntity firstMockReturn = firstLockId.equals(dummyUserId) ? activeUser : inactiveReceiver;
    UserEntity secondMockReturn = secondLockId.equals(dummyUserId) ? activeUser : inactiveReceiver;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.of(firstMockReturn));
    when(userRepository.findByIdForUpdate(secondLockId)).thenReturn(Optional.of(secondMockReturn));

    BigDecimal amount = BigDecimal.valueOf(10.00);
    assertThrows(ReceiverUserInactiveException.class,
        () -> transferAmountService.transferAmount(dummyUserId, receiverId, amount));

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar NegativeAmountException quando o remetente não tiver saldo suficiente")
  void shouldThrowNegativeAmountExceptionWhenSenderHasInsufficientBalance() {
    UUID receiverId = UUID.randomUUID();
    UserEntity receiverUser = UserEntity.builder()
        .id(receiverId)
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .active(true)
        .build();

    BigDecimal expensiveAmount = new BigDecimal("300.00");

    UUID firstLockId = dummyUserId.compareTo(receiverId) < 0 ? dummyUserId : receiverId;
    UUID secondLockId = firstLockId.equals(dummyUserId) ? receiverId : dummyUserId;

    UserEntity firstMockReturn = firstLockId.equals(dummyUserId) ? activeUser : receiverUser;
    UserEntity secondMockReturn = secondLockId.equals(dummyUserId) ? activeUser : receiverUser;

    when(userRepository.findByIdForUpdate(firstLockId)).thenReturn(Optional.of(firstMockReturn));
    when(userRepository.findByIdForUpdate(secondLockId)).thenReturn(Optional.of(secondMockReturn));

    assertThrows(NegativeAmountException.class,
        () -> transferAmountService.transferAmount(dummyUserId, receiverId, expensiveAmount));

    verify(userRepository, never()).save(any(UserEntity.class));
  }
}
