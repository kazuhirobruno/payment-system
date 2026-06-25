package br.com.kazuhiro.payment_system.modules.user.usecases;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.ChangePasswordRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChangePasswordUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void execute(ChangePasswordRequestDTO changePasswordRequestDTO, String id) {
    var user = this.userRepository.findById(UUID.fromString(id)).orElseThrow(() -> {
      throw new UserNotFoundException();
    });

    if (!user.isActive()) {
      throw new UserNotFoundException();
    }

    if (!changePasswordRequestDTO.getPassword().equals(changePasswordRequestDTO.getConfirmPassword())) {
      throw new PasswordNotMatchesException();
    }

    String encodedPassword = passwordEncoder.encode(changePasswordRequestDTO.getPassword());
    user.setPassword(encodedPassword);
    this.userRepository.save(user);
  }
}
