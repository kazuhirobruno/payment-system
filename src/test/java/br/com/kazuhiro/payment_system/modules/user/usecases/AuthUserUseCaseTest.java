package br.com.kazuhiro.payment_system.modules.user.usecases;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import javax.security.sasl.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUserUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthUserUseCase authUserUseCase;

  private final String secretKeyMock = "my-secret-key-for-testing-purposes-only";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authUserUseCase, "secretkey", secretKeyMock);
  }

  @Test
  @DisplayName("Deve autenticar o usuário com sucesso e retornar o token JWT quando ativo")
  void shouldAuthenticateUserWithSuccess() throws AuthenticationException {
    AuthUserRequestDTO request = new AuthUserRequestDTO("user@email.com", "password123");
    UUID userId = UUID.randomUUID();

    UserEntity user = UserEntity.builder()
        .id(userId)
        .email("user@email.com")
        .password("encodedPassword123")
        .active(true)
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

    AuthUserResponseDTO response = authUserUseCase.execute(request);

    assertNotNull(response);
    assertNotNull(response.getToken());
    assertNotNull(response.getExpiresAt());
    assertTrue(response.getRoles().contains("USER"));

    DecodedJWT decodedJWT = JWT.decode(response.getToken());
    assertEquals("payment-system", decodedJWT.getIssuer());
    assertEquals(userId.toString(), decodedJWT.getSubject());
    assertEquals("USER", decodedJWT.getClaim("roles").asList(String.class).get(0));

    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
  }

  @Test
  @DisplayName("Deve lançar DeletedUserLoginException quando o usuário estiver inativo (Soft Delete)")
  void shouldThrowDeletedUserLoginExceptionWhenUserIsInactive() {
    AuthUserRequestDTO request = new AuthUserRequestDTO("deleted@email.com", "password123");

    UserEntity user = UserEntity.builder()
        .email("deleted@email.com")
        .password("encodedPassword123")
        .active(false)
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

    assertThrows(DeletedUserLoginException.class, () -> {
      authUserUseCase.execute(request);
    });

    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
  }

  @Test
  @DisplayName("Deve lançar UsernameNotFoundException quando o e-mail não existir")
  void shouldThrowExceptionWhenEmailNotFound() {
    AuthUserRequestDTO request = new AuthUserRequestDTO("notfound@email.com", "password123");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
      authUserUseCase.execute(request);
    });

    assertEquals("Email/senha incorreto.", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, never()).matches(anyString(), anyString());
  }

  @Test
  @DisplayName("Deve lançar AuthenticationException quando a senha estiver incorreta")
  void shouldThrowExceptionWhenPasswordDoesNotMatch() {
    AuthUserRequestDTO request = new AuthUserRequestDTO("user@email.com", "wrongPassword");
    UserEntity user = UserEntity.builder()
        .email("user@email.com")
        .password("encodedPassword123")
        .active(true)
        .build();

    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

    AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
      authUserUseCase.execute(request);
    });

    assertEquals("Email/senha incorreto.", exception.getMessage());
    verify(userRepository, times(1)).findByEmail(request.getEmail());
    verify(passwordEncoder, times(1)).matches(request.getPassword(), user.getPassword());
  }
}
