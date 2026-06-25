package br.com.kazuhiro.payment_system.modules.transactions.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de requisição para operações que exigem apenas a informação de valor")
public class TransactionAmountRequestDTO {

  @NotNull(message = "O valor da operação é obrigatório.")
  @Positive(message = "O valor deve ser obrigatoriamente maior que zero.")
  @Schema(description = "Valor monetário a ser processado na operação", example = "100.50", minimum = "0.01")
  private BigDecimal amount;
}
