package br.com.kazuhiro.payment_transfer_api.modules.transactions.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Schema(description = "Registra a transferência de valores entre duas contas")
public class Transaction {

  @Id
  @NotNull(message = "O ID da transação é obrigatório")
  @Schema(description = "Identificador único da transação", example = "9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID id;

  @NotNull(message = "O ID do pagador é obrigatório")
  @Schema(description = "ID do usuário que está enviando o dinheiro", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID senderId;

  @NotNull(message = "O ID do recebedor é obrigatório")
  @Schema(description = "ID do usuário que está recebendo o dinheiro", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID receiverId;

  @NotNull(message = "O valor da transação é obrigatório")
  @DecimalMin(value = "0.01", message = "O valor mínimo para transferência é R$ 0.01")
  @Schema(description = "Valor transferido", example = "250.00", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal amount;

  @NotNull(message = "A data de criação é obrigatória")
  @PastOrPresent(message = "A data da transação não pode estar no futuro")
  @Schema(description = "Data e hora em que a transação foi realizada", example = "2026-06-23T11:57:00", requiredMode = Schema.RequiredMode.REQUIRED)
  private LocalDateTime createdAt;
}