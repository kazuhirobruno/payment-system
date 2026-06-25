package br.com.kazuhiro.payment_system.modules.transactions.dtos;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de requisição para transferência de valores entre contas de usuários")
public class TransferRequestDTO {

  @NotNull(message = "O identificador do destinatário é obrigatório.")
  @Schema(description = "ID único (UUID) do usuário beneficiário que receberá o valor", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID receiverId;

  @NotNull(message = "O valor da transferência é obrigatório.")
  @Positive(message = "O valor deve ser obrigatoriamente maior que zero.")
  @Digits(integer = 17, fraction = 2, message = "O valor excede o limite numérico permitido de 17 dígitos inteiros e 2 casas decimais.")
  @Schema(description = "Valor monetário a ser transferido", example = "75.50", minimum = "0.01", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal amount;
}