package br.com.kazuhiro.payment_system.modules.transactions.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.types.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction")
@Schema(description = "Representa o registro de qualquer movimentação financeira (Depósito, Saque ou Transferência)")
public class TransactionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(nullable = false)
  @Schema(description = "Identificador único da transação gerado automaticamente", example = "9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c")
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Schema(description = "Tipo da operação financeira realizada", example = "TRANSFER", allowableValues = { "DEPOSIT",
      "WITHDRAW", "TRANSFER" })
  private TransactionType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = true)
  @Schema(description = "Usuário de origem (pagador/remetente). Será NULO caso o tipo de transação seja DEPOSIT.", nullable = true)
  private UserEntity sender;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "receiver_id", nullable = true)
  @Schema(description = "Usuário de destino (recebedor/beneficiário). Será NULO caso o tipo de transação seja WITHDRAW.", nullable = true)
  private UserEntity receiver;

  @Column(nullable = false)
  @Schema(description = "Valor monetário total movimentado na operação", example = "250.00", minimum = "0.01")
  private BigDecimal amount;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  @Schema(description = "Data e hora exata em que a transação foi processada", example = "2026-06-25T14:15:00Z")
  private Instant createdAt;
}