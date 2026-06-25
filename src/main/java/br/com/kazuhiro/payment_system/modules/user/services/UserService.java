package br.com.kazuhiro.payment_system.modules.user.services;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.NegativeAmountException;
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

  public UserEntity withdrawAmount(UUID userId, BigDecimal amount) {
    UserEntity user = userRepository.findByIdForUpdate(userId).orElseThrow(() -> new UserNotFoundException());
    if (!user.isActive()) {
      throw new DeletedUserLoginException();
    }
    BigDecimal newBalance = user.getBalance().subtract(amount);
    if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new NegativeAmountException();
    }
    user.setBalance(newBalance);
    userRepository.save(user);
    return user;
  }
}