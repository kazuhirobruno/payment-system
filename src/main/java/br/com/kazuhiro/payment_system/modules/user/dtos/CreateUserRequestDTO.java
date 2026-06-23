package br.com.kazuhiro.payment_system.modules.user.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Objeto de transferência de dados para requisição de criação/atualização de Usuário")
public class CreateUserRequestDTO {

  @NotBlank(message = "O nome não pode estar em branco")
  @Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
  @Schema(description = "Nome completo do usuário", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "O e-mail não pode estar em branco")
  @Email(message = "O e-mail deve ser válido")
  @Schema(description = "Endereço de e-mail do usuário", example = "joao@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @NotBlank(message = "A senha não pode estar em branco")
  @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
  @Schema(description = "Senha de acesso do usuário", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

  @NotBlank(message = "A confirmação de senha não pode estar em branco")
  @Schema(description = "Confirmação da senha de acesso", example = "senha123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String confirmPassword;

  @NotNull(message = "O saldo inicial é obrigatório")
  @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo")
  @Schema(description = "Saldo inicial de depósito para abertura da conta do usuário", example = "100.00", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal balance;
}
