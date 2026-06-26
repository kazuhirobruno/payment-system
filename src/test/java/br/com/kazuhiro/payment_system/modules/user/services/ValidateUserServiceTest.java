package br.com.kazuhiro.payment_system.modules.user.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ValidateUserServiceTest {
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private ValidateUserService validateUserService;

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
  @DisplayName("Deve validar com sucesso quando o usuário existir e estiver ativo")
  void shouldValidateUserExistsWithSuccess() {
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.of(activeUser));

    assertDoesNotThrow(() -> validateUserService.validateUserExists(dummyUserId));

    verify(userRepository, times(1)).findById(dummyUserId);
  }

  @Test
  @DisplayName("Deve lançar UserNotFoundException na validação quando o ID do usuário não existir no banco")
  void shouldThrowUserNotFoundExceptionOnValidation() {
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> validateUserService.validateUserExists(dummyUserId));

    verify(userRepository, times(1)).findById(dummyUserId);
  }

  @Test
  @DisplayName("Deve lançar DeletedUserLoginException na validação quando o usuário existir mas estiver inativo")
  void shouldThrowDeletedUserLoginExceptionOnValidation() {
    when(userRepository.findById(dummyUserId)).thenReturn(Optional.of(inactiveUser));

    assertThrows(DeletedUserLoginException.class, () -> validateUserService.validateUserExists(dummyUserId));

    verify(userRepository, times(1)).findById(dummyUserId);
  }

}
