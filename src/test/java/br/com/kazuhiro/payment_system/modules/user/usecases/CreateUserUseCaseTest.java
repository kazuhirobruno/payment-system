package br.com.kazuhiro.payment_system.modules.user.usecases;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CreateUserUseCase createUserUseCase;

  @SuppressWarnings("null")
  @Test
  @DisplayName("Deve criar um usuário com sucesso quando os dados forem válidos")
  void shouldCreateUserWithSuccess() {
    CreateUserRequestDTO request = CreateUserRequestDTO.builder()
        .name("John Doe")
        .email("john@example.com")
        .password("password123")
        .confirmPassword("password123")
        .balance(new BigDecimal("100.0"))
        .build();

    UserEntity savedUser = UserEntity.builder()
        .id(UUID.randomUUID())
        .name("John Doe")
        .email("john@example.com")
        .password("encoded_password")
        .active(true)
        .balance(new BigDecimal("100.0"))
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
    when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

    CreateUserResponseDTO response = createUserUseCase.execute(request);

    assertNotNull(response);
    assertEquals(savedUser.getId(), response.getId());
    assertEquals(request.getName(), response.getName());
    assertEquals(request.getEmail(), response.getEmail());
    assertEquals(request.getBalance(), response.getBalance());

    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, times(1)).encode(request.getPassword());
    verify(userRepository, times(1)).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar UserFoundException quando o e-mail já estiver cadastrado")
  void shouldThrowUserFoundExceptionWhenEmailAlreadyExists() {
    CreateUserRequestDTO request = CreateUserRequestDTO.builder()
        .email("existing@example.com")
        .build();

    UserEntity existingUser = UserEntity.builder().build();
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingUser));

    assertThrows(UserFoundException.class, () -> createUserUseCase.execute(request));

    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Deve lançar PasswordNotMatchesException quando a senha e a confirmação forem diferentes")
  void shouldThrowPasswordNotMatchesExceptionWhenPasswordsDoNotMatch() {
    CreateUserRequestDTO request = CreateUserRequestDTO.builder()
        .email("john@example.com")
        .password("password123")
        .confirmPassword("differentPassword")
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThrows(PasswordNotMatchesException.class, () -> createUserUseCase.execute(request));

    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }
}
