package br.com.kazuhiro.payment_system.modules.user.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de transferência de dados que encapsula as informações de perfil e saldo do usuário autenticado")
public class UserProfileResponseDTO {

  @Schema(description = "Nome completo do usuário cadastrado no sistema", example = "John Doe")
  private String name;

  @Schema(description = "Endereço de e-mail associado à conta do usuário", example = "john.doe@example.com")
  private String email;

  @Schema(description = "Saldo financeiro disponível atualmente na carteira digital do usuário", example = "250.50", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal balance;
}