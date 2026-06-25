package br.com.kazuhiro.payment_system.modules.user.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private UUID dummyUserId;
  private UserEntity activeUser;
  private UserEntity inactiveUser;

  @BeforeEach
  void setUp() {
    dummyUserId = UUID.randomUUID();

    activeUser = UserEntity.builder()
        .id(dummyUserId)
        .name("Carlos Oliveira")
        .email("carlos@email.com")
        .balance(new BigDecimal("200.00"))
        .active(true)
        .build();

    inactiveUser = UserEntity.builder()
        .id(dummyUserId)
        .name("Carlos Oliveira")
        .email("carlos@email.com")
        .balance(new BigDecimal("200.00"))
        .active(false)
        .build();
  }

  @Test
  @DisplayName("Deve somar o valor ao saldo com sucesso quando o usuário estiver ativo")
  void shouldAddBalanceWithSuccessWhenUserIsActive() {
    BigDecimal amountToDeposit = new BigDecimal("150.50");
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.of(activeUser));
    when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity updatedUser = userService.addBalance(dummyUserId, amountToDeposit);

    assertNotNull(updatedUser);
    assertEquals(new BigDecimal("350.50"), updatedUser.getBalance());
    verify(userRepository, times(1)).findById(dummyUserId);
    verify(userRepository, times(1)).save(activeUser);
  }

  @Test
  @DisplayName("Deve lançar UserNotFoundException quando o ID do usuário não existir no banco")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    BigDecimal amountToDeposit = new BigDecimal("50.00");
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      userService.addBalance(dummyUserId, amountToDeposit);
    });

    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar DeletedUserLoginException quando o usuário for encontrado mas estiver inativo")
  void shouldThrowDeletedUserLoginExceptionWhenUserIsInactive() {
    BigDecimal amountToDeposit = new BigDecimal("100.00");
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.of(inactiveUser));

    assertThrows(DeletedUserLoginException.class, () -> {
      userService.addBalance(dummyUserId, amountToDeposit);
    });

    verify(userRepository, never()).save(any(UserEntity.class));
  }
}