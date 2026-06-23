package br.com.kazuhiro.payment_transfer_api.modules.account.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.kazuhiro.payment_transfer_api.modules.user.entities.User;

@Entity
@Data
@Schema(description = "Representa a conta bancária de um usuário")
public class Account {

  @Id
  @NotNull(message = "O ID da conta é obrigatório")
  @Schema(description = "Identificador único da conta", example = "7b9e4a32-1123-4c55-b89d-8c1122334455", requiredMode = Schema.RequiredMode.REQUIRED)
  private UUID id;

  @NotNull(message = "O saldo não pode ser nulo")
  @DecimalMin(value = "0.00", message = "O saldo inicial não pode ser negativo")
  @Schema(description = "Saldo atual da conta", example = "1500.50", requiredMode = Schema.RequiredMode.REQUIRED)
  private BigDecimal balance;

  @OneToOne
  @JoinColumn(name = "user_id")
  @NotNull(message = "A conta deve estar vinculada a um usuário")
  @Schema(description = "Usuário proprietário desta conta", requiredMode = Schema.RequiredMode.REQUIRED)
  private User user;
}