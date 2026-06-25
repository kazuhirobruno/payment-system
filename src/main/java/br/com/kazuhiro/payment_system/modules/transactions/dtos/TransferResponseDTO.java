package br.com.kazuhiro.payment_system.modules.transactions.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Dados de retorno que servem como comprovante de uma transferência concluída")
public class TransferResponseDTO {

  @Schema(description = "Identificador único da transação gerada no banco", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
  private UUID transactionId;

  @Schema(description = "Tipo da operação realizada", example = "TRANSFER")
  private String type;

  @Schema(description = "Valor total que foi transferido", example = "75.50")
  private BigDecimal amount;

  @Schema(description = "Saldo atualizado do pagador após o débito da transferência", example = "424.50")
  private BigDecimal newBalance;

  @Schema(description = "ID único (UUID) do usuário que recebeu o dinheiro", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  private UUID receiverId;

  @Schema(description = "Nome do usuário beneficiário que recebeu o dinheiro", example = "João Silva")
  private String receiverName;

  @Schema(description = "Data e hora exata em que a transferência foi processada", example = "2026-06-25T21:30:00Z")
  private Instant createdAt;
}