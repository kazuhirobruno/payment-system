package br.com.kazuhiro.payment_system.modules.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Objeto de transferência de dados contendo as informações necessárias para a alteração de senha do usuário")
public class ChangePasswordRequestDTO {

  @NotBlank(message = "A senha não pode estar em branco")
  @Size(min = 6, max = 20, message = "A senha deve ter entre 6 e 20 caracteres")
  @Schema(description = "Nova senha de acesso que será cadastrada para o usuário", example = "SecurePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;

  @NotBlank(message = "A confirmação de senha não pode estar em branco")
  @Schema(description = "Confirmação exata da nova senha de acesso informada", example = "SecurePassword123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String confirmPassword;
}