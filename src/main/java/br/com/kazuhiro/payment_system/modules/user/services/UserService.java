package br.com.kazuhiro.payment_system.modules.user.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.NegativeAmountException;
import br.com.kazuhiro.payment_system.exceptions.ReceiverUserInactiveException;
import br.com.kazuhiro.payment_system.exceptions.SameAccountTransferException;
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

  public void validateUserExists(UUID userId) {
    UserEntity user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException());
    if (!user.isActive()) {
      throw new DeletedUserLoginException();
    }
  }

  public List<UserEntity> transferAmount(UUID userId, UUID receiverId, BigDecimal amount) {
    if (userId.equals(receiverId)) {
      throw new SameAccountTransferException();
    }

    UUID firstLockId = userId.compareTo(receiverId) < 0 ? userId : receiverId;
    UUID secondLockId = firstLockId.equals(userId) ? receiverId : userId;

    UserEntity firstUser = userRepository.findByIdForUpdate(firstLockId)
        .orElseThrow(() -> new UserNotFoundException());
    UserEntity secondUser = userRepository.findByIdForUpdate(secondLockId)
        .orElseThrow(() -> new UserNotFoundException());

    UserEntity sender = firstUser.getId().equals(userId) ? firstUser : secondUser;
    UserEntity receiver = firstUser.getId().equals(receiverId) ? firstUser : secondUser;

    if (!sender.isActive()) {
      throw new DeletedUserLoginException();
    }

    if (!receiver.isActive()) {
      throw new ReceiverUserInactiveException();
    }

    BigDecimal newSenderBalance = sender.getBalance().subtract(amount);
    if (newSenderBalance.compareTo(BigDecimal.ZERO) < 0) {
      throw new NegativeAmountException();
    }
    sender.setBalance(newSenderBalance);
    receiver.setBalance(receiver.getBalance().add(amount));
    userRepository.save(sender);
    userRepository.save(receiver);

    return List.of(sender, receiver);
  }
}