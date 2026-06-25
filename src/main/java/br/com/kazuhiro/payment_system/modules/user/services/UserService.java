package br.com.kazuhiro.payment_system.modules.user.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;

  public UserEntity addBalance(UUID userId, BigDecimal amount) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
    if (!user.isActive()) {
      throw new DeletedUserLoginException();
    }
    user.setBalance(user.getBalance().add(amount));
    userRepository.save(user);
    return user;
  }
}