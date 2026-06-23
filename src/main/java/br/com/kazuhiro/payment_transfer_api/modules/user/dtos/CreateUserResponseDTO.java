package br.com.kazuhiro.payment_transfer_api.modules.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de resposta retornado após a criação bem-sucedida de um Usuário")
public class CreateUserResponseDTO {

  @Schema(description = "Identificador único do usuário criado", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  private UUID id;

  @Schema(description = "Nome completo do usuário cadastrado", example = "João Silva")
  private String name;

  @Schema(description = "Endereço de e-mail do usuário cadastrado", example = "joao@email.com")
  private String email;

  @Schema(description = "Saldo inicial de depósito para abertura da conta do usuário", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal balance;
}