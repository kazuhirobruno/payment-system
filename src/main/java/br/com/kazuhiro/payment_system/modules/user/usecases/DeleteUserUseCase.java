package br.com.kazuhiro.payment_system.modules.user.usecases;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void delete(String id) {
    var user = this.userRepository.findById(UUID.fromString(id)).orElseThrow(() -> {
      throw new UserNotFoundException();
    });

    Long timestamp = Instant.now().toEpochMilli();
    user.setActive(false);
    user.setName("DELETED_" + timestamp);
    user.setEmail("DELETED_" + timestamp + "@deleted.com");
    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

    this.userRepository.save(user);
  }
}
