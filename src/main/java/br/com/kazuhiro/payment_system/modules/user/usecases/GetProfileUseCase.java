package br.com.kazuhiro.payment_system.modules.user.usecases;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.UserProfileResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetProfileUseCase {
  private final UserRepository userRepository;

  public UserProfileResponseDTO execute(String id) {
    var user = this.userRepository.findById(UUID.fromString(id)).orElseThrow(() -> {
      throw new UserNotFoundException();
    });

    return UserProfileResponseDTO.builder().balance(user.getBalance()).email(user.getEmail()).name(user.getName())
        .build();
  }
}
