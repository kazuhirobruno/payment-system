package br.com.kazuhiro.payment_system.modules.user.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.ChangePasswordRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private ChangePasswordUseCase changePasswordUseCase;

  private UUID sampleUserId;
  private String sampleUserIdString;
  private ChangePasswordRequestDTO validRequestDTO;

  @BeforeEach
  void setUp() {
    this.sampleUserId = UUID.randomUUID();
    this.sampleUserIdString = sampleUserId.toString();
    this.validRequestDTO = ChangePasswordRequestDTO.builder()
        .password("NewSecurePassword123")
        .confirmPassword("NewSecurePassword123")
        .build();
  }

  @Test
  @DisplayName("Should update user password successfully when request data and identifier are valid")
  void shouldUpdateUserPasswordSuccessfullyWhenRequestDataAndIdentifierAreValid() {
    UserEntity existingUserEntity = UserEntity.builder()
        .id(sampleUserId)
        .name("John Doe")
        .email("john.doe@example.com")
        .password("OldCryptedPassword")
        .active(true)
        .build();

    when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(existingUserEntity));
    when(passwordEncoder.encode(validRequestDTO.getPassword())).thenReturn("CryptedNewSecurePassword123");
    when(userRepository.save(any(UserEntity.class))).thenReturn(existingUserEntity);

    assertDoesNotThrow(() -> changePasswordUseCase.execute(validRequestDTO, sampleUserIdString));

    assertEquals("CryptedNewSecurePassword123", existingUserEntity.getPassword());
    verify(userRepository, times(1)).findById(sampleUserId);
    verify(passwordEncoder, times(1)).encode(validRequestDTO.getPassword());
    verify(userRepository, times(1)).save(existingUserEntity);
  }

  @Test
  @DisplayName("Should throw PasswordNotMatchesException when password and confirmation do not match")
  void shouldThrowPasswordNotMatchesExceptionWhenPasswordAndConfirmationDoNotMatch() {
    ChangePasswordRequestDTO invalidRequestDTO = ChangePasswordRequestDTO.builder()
        .password("NewSecurePassword123")
        .confirmPassword("DifferentPassword123")
        .build();

    UserEntity existingUserEntity = UserEntity.builder().id(sampleUserId).active(true).build();
    when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(existingUserEntity));

    assertThrows(PasswordNotMatchesException.class, () -> {
      changePasswordUseCase.execute(invalidRequestDTO, sampleUserIdString);
    });

    verify(userRepository, times(1)).findById(sampleUserId);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user identifier does not exist in database")
  void shouldThrowUserNotFoundExceptionWhenUserIdentifierDoesNotExistInDatabase() {
    when(userRepository.findById(sampleUserId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      changePasswordUseCase.execute(validRequestDTO, sampleUserIdString);
    });

    verify(userRepository, times(1)).findById(sampleUserId);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when trying to change password of an inactive user account")
  void shouldThrowUserNotFoundExceptionWhenTryingToChangePasswordOfAnInactiveUserAccount() {
    UserEntity inactiveUserEntity = UserEntity.builder()
        .id(sampleUserId)
        .name("John Doe")
        .email("john.doe@example.com")
        .password("OldCryptedPassword")
        .active(false)
        .build();

    when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(inactiveUserEntity));

    assertThrows(UserNotFoundException.class, () -> {
      changePasswordUseCase.execute(validRequestDTO, sampleUserIdString);
    });

    verify(userRepository, times(1)).findById(sampleUserId);
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).save(any(UserEntity.class));
  }
}