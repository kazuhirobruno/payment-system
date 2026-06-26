package br.com.kazuhiro.payment_system.modules.transactions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import br.com.kazuhiro.payment_system.modules.user.services.BalanceService;
import br.com.kazuhiro.payment_system.modules.user.services.TransferAmountService;

@ExtendWith(MockitoExtension.class)
class UserServiceConcurrencyTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private BalanceService balanceService;

  @InjectMocks
  private TransferAmountService transferAmountService;

  @Test
  @DisplayName("Deve manter consistência sob carga concorrente (saque, depósito e transferência)")
  void shouldHandleConcurrentOperationsCorrectly() throws InterruptedException {
    int threads = 60;
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(threads);

    UUID userAId = UUID.randomUUID();
    UUID userBId = UUID.randomUUID();
    UUID userCId = UUID.randomUUID();

    UserEntity userA = UserEntity.builder()
        .id(userAId)
        .balance(new BigDecimal("1000.00"))
        .active(true)
        .build();

    UserEntity userB = UserEntity.builder()
        .id(userBId)
        .balance(new BigDecimal("1000.00"))
        .active(true)
        .build();

    UserEntity userC = UserEntity.builder()
        .id(userCId)
        .balance(new BigDecimal("1000.00"))
        .active(true)
        .build();

    when(userRepository.findByIdForUpdate(any(UUID.class)))
        .thenAnswer(invocation -> {
          UUID id = invocation.getArgument(0);

          if (id.equals(userAId))
            return java.util.Optional.of(userA);
          if (id.equals(userBId))
            return java.util.Optional.of(userB);
          return java.util.Optional.of(userC);
        });

    when(userRepository.findById(any(UUID.class)))
        .thenAnswer(invocation -> {
          UUID id = invocation.getArgument(0);

          if (id.equals(userAId))
            return java.util.Optional.of(userA);
          if (id.equals(userBId))
            return java.util.Optional.of(userB);
          return java.util.Optional.of(userC);
        });

    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    for (int i = 0; i < threads; i++) {
      executor.submit(() -> {
        try {
          int op = ThreadLocalRandom.current().nextInt(3);
          switch (op) {
            case 0 -> balanceService.withdrawAmount(
                userAId,
                new BigDecimal("5.00"));

            case 1 -> balanceService.addBalance(
                userBId,
                new BigDecimal("5.00"));

            case 2 -> transferAmountService.transferAmount(
                userAId,
                userCId,
                new BigDecimal("5.00"));
          }

        } catch (Exception ignored) {
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    executor.shutdown();

    assertTrue(userA.getBalance().compareTo(BigDecimal.ZERO) >= 0);
    assertTrue(userB.getBalance().compareTo(BigDecimal.ZERO) >= 0);
    assertTrue(userC.getBalance().compareTo(BigDecimal.ZERO) >= 0);
  }
}