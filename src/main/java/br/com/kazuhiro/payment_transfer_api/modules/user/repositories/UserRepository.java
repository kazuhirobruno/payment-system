package br.com.kazuhiro.payment_transfer_api.modules.user.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.kazuhiro.payment_transfer_api.modules.user.entities.UserEntity;
import lombok.NonNull;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByEmail(@NonNull String email);
}
