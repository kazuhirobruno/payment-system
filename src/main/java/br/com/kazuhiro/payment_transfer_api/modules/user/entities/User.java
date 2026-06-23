package br.com.kazuhiro.payment_transfer_api.modules.user.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@Table(name = "user")
@Schema(description = "Representa um usuário cadastrado no sistema de pagamentos")
public class User {

  @Id
  @NotNull(message = "O ID é obrigatório")
  @Schema(description = "Identificador único do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID id;

  @NotBlank(message = "O nome não pode estar em branco")
  @Size(min = 3, max = 255, message = "O nome deve ter entre 3 e 255 caracteres")
  @Schema(description = "Nome completo do usuário", example = "João Silva", requiredMode = Schema.RequiredMode.REQUIRED)
  private String name;

  @NotBlank(message = "O e-mail não pode estar em branco")
  @Email(message = "O e-mail deve ser válido")
  @Schema(description = "Endereço de e-mail do usuário", example = "joao@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @NotBlank(message = "O CPF não pode estar em branco")
  @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 dígitos numéricos")
  @Schema(description = "Cadastro de Pessoa Física (CPF), apenas números", example = "12345678901", requiredMode = Schema.RequiredMode.REQUIRED)
  private String cpf;

  @NotNull(message = "O status ativo é obrigatório")
  @Schema(description = "Status de ativação do usuário no sistema", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean active;
}