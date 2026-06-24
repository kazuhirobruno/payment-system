package br.com.kazuhiro.payment_system.modules.user.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados necessários para autenticar um usuário no sistema")
public class AuthUserRequestDTO {

  @Schema(description = "E-mail cadastrado do usuário", example = "usuario@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(description = "Senha secreta do usuário", example = "Senha@123", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;
}