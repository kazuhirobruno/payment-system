package br.com.kazuhiro.payment_system.modules.transactions.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import br.com.kazuhiro.payment_system.types.TransactionType;

@Data
@Builder
@Schema(description = "Dados de retorno que servem como comprovante da operação realizada")
public class TransactionResponseDTO {

  @Schema(description = "Identificador único da transação gerada", example = "9f8e7d6c-5b4a-3f2e-1d0c-9b8a7f6e5d4c")
  private UUID transactionId;

  @Schema(description = "Tipo da operação realizada", example = "DEPOSIT")
  private TransactionType type;

  @Schema(description = "Valor que foi movimentado na operação", example = "100.50")
  private BigDecimal amount;

  @Schema(description = "Saldo atualizado da conta do usuário após a operação", example = "600.50")
  private BigDecimal newBalance;

  @Schema(description = "Data e hora exata em que a operação foi concluída", example = "2026-06-25T14:35:00Z")
  private Instant createdAt;
}