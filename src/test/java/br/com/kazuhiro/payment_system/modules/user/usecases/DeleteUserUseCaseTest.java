package br.com.kazuhiro.payment_system.modules.user.usecases;

import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity; // Ajuste o pacote da sua entidade conforme necessário
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Caso de Uso - Deletar Usuário (DeleteUserUseCase)")
class DeleteUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private DeleteUserUseCase deleteUserUseCase;

  @Test
  @DisplayName("1. Deve inativar e anonimizar os dados do usuário com sucesso quando o ID for válido")
  void shouldDeleteAndAnonymizeUserSuccessfully() {
    // Arrange
    UUID userId = UUID.randomUUID();
    String userIdStr = userId.toString();

    UserEntity user = new UserEntity();
    user.setId(userId);
    user.setActive(true);
    user.setName("John Doe");
    user.setEmail("john.doe@example.com");
    user.setPassword("old-hashed-password");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(anyString())).thenReturn("new-random-hashed-password");

    deleteUserUseCase.delete(userIdStr);

    ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository, times(1)).save(userCaptor.capture());

    UserEntity savedUser = userCaptor.getValue();
    assertNotNull(savedUser);
    assertFalse(savedUser.isActive(), "O usuário deveria ter sido marcado como inativo (active = false)");
    assertTrue(savedUser.getName().startsWith("DELETED_"), "O nome deveria começar com DELETED_");
    assertTrue(savedUser.getEmail().startsWith("DELETED_"), "O e-mail deveria começar com DELETED_");
    assertTrue(savedUser.getEmail().endsWith("@deleted.com"), "O e-mail deveria terminar com @deleted.com");
    assertEquals("new-random-hashed-password", savedUser.getPassword(),
        "A senha deveria ter sido atualizada pelo encoder");

    verify(userRepository, times(1)).findById(userId);
    verify(passwordEncoder, times(1)).encode(anyString());
  }

  @Test
  @DisplayName("2. Deve lançar UserNotFoundException quando o usuário não existir no banco de dados")
  void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
    UUID userId = UUID.randomUUID();
    String userIdStr = userId.toString();

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> deleteUserUseCase.delete(userIdStr));

    verify(userRepository, times(1)).findById(userId);
    verifyNoMoreInteractions(passwordEncoder, userRepository);
  }

  @Test
  @DisplayName("3. Deve lançar IllegalArgumentException quando o formato do ID enviado não for um UUID válido")
  void shouldThrowIllegalArgumentExceptionWhenIdIsInvalidUuidFormat() {
    String invalidUuidStr = "id-com-formato-invalido";

    assertThrows(IllegalArgumentException.class, () -> deleteUserUseCase.delete(invalidUuidStr));

    verifyNoInteractions(userRepository, passwordEncoder);
  }
}