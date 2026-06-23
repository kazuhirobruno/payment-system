package br.com.kazuhiro.payment_transfer_api.modules.transactions.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.com.kazuhiro.payment_transfer_api.modules.user.entities.UserEntity;

@Entity
@Data
@Schema(description = "Registra a transferência financeira realizada de um usuário para outro")
public class TransactionEntity {

  @Id
  @Column(nullable = false)
  @Schema(description = "Identificador único da transação", example = "9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  @Schema(description = "Usuário de origem (pagador) que enviou o valor")
  private UserEntity sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = false)
  @Schema(description = "Usuário de destino (recebedor) que recebeu o valor")
  private UserEntity receiver;

  @Column(nullable = false)
  @Schema(description = "Valor total transferido na operação", example = "150.50")
  private BigDecimal amount;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  @Schema(description = "Data e hora em que a transferência foi efetuada", example = "2026-06-23T18:53:00Z")
  private Instant createdAt;
}