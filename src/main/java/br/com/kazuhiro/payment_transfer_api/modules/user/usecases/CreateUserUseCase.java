package br.com.kazuhiro.payment_transfer_api.modules.user.usecases;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_transfer_api.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_transfer_api.exceptions.UserFoundException;
import br.com.kazuhiro.payment_transfer_api.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_transfer_api.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_transfer_api.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_transfer_api.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @SuppressWarnings("null")
  public CreateUserResponseDTO execute(CreateUserRequestDTO createUserRequestDTO) {
    this.userRepository.findByEmail(createUserRequestDTO.getEmail()).ifPresent(user -> {
      throw new UserFoundException();
    });

    if (!createUserRequestDTO.getPassword().equals(createUserRequestDTO.getConfirmPassword())) {
      throw new PasswordNotMatchesException();
    }

    String encodedPassword = passwordEncoder.encode(createUserRequestDTO.getPassword());
    UserEntity userEntity = UserEntity.builder()
        .name(createUserRequestDTO.getName())
        .password(encodedPassword)
        .email(createUserRequestDTO.getEmail())
        .active(true)
        .balance(createUserRequestDTO.getBalance())
        .build();

    UserEntity response = this.userRepository.save(userEntity);
    return CreateUserResponseDTO.builder()
        .balance(response.getBalance())
        .email(response.getEmail())
        .id(response.getId())
        .name(response.getName())
        .build();
  }
}
