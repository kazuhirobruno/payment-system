package br.com.kazuhiro.payment_system.modules.user.services;

import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateUserService {
  private final UserRepository userRepository;

  public void validateUserExists(UUID userId) {
    UserEntity user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    if (!user.isActive()) {
      throw new DeletedUserLoginException();
    }
  }
}
