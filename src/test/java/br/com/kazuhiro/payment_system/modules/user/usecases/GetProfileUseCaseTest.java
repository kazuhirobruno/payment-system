package br.com.kazuhiro.payment_system.modules.user.usecases;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.UserProfileResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetProfileUseCaseTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private GetProfileUseCase getProfileUseCase;

  private UUID sampleUserId;
  private String sampleUserIdString;

  @BeforeEach
  void setUp() {
    this.sampleUserId = UUID.randomUUID();
    this.sampleUserIdString = sampleUserId.toString();
  }

  @Test
  @DisplayName("Should return user profile data when user identifier exists in database")
  void shouldReturnUserProfileDataWhenUserIdentifierExistsInDatabase() {
    UserEntity expectedUserEntity = UserEntity.builder()
        .id(sampleUserId)
        .name("John Doe")
        .email("john.doe@example.com")
        .password("SecurePassword123")
        .balance(new BigDecimal("250.50"))
        .active(true)
        .build();

    when(userRepository.findById(sampleUserId)).thenReturn(Optional.of(expectedUserEntity));

    UserProfileResponseDTO resultResponseDTO = getProfileUseCase.execute(sampleUserIdString);

    assertNotNull(resultResponseDTO);
    assertEquals(expectedUserEntity.getName(), resultResponseDTO.getName());
    assertEquals(expectedUserEntity.getEmail(), resultResponseDTO.getEmail());
    assertEquals(expectedUserEntity.getBalance(), resultResponseDTO.getBalance());

    verify(userRepository, times(1)).findById(sampleUserId);
  }

  @Test
  @DisplayName("Should throw UserNotFoundException when user identifier does not exist in database")
  void shouldThrowUserNotFoundExceptionWhenUserIdentifierDoesNotExistInDatabase() {
    when(userRepository.findById(sampleUserId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      getProfileUseCase.execute(sampleUserIdString);
    });

    verify(userRepository, times(1)).findById(sampleUserId);
  }
}