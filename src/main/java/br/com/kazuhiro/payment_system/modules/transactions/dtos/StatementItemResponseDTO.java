package br.com.kazuhiro.payment_system.modules.transactions.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Schema(description = "Detalhes de uma movimentação no extrato financeiro")
public class StatementItemResponseDTO {

  @Schema(description = "ID único da transação", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
  private UUID transactionId;

  @Schema(description = "Tipo da operação (DEPOSIT, WITHDRAW, TRANSFER)", example = "TRANSFER")
  private String type;

  @Schema(description = "Valor movimentado", example = "50.00")
  private BigDecimal amount;

  @Schema(description = "Data e hora do processamento", example = "2026-06-25T14:35:00Z")
  private Instant createdAt;

  @Schema(description = "Nome do outro envolvido na transação (se houver)", example = "Jane Doe")
  private String counterpartName;
}