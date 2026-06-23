package br.com.kazuhiro.payment_transfer_api.modules.user.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
@Schema(description = "Representa um usuário cadastrado no sistema com seus dados financeiros e de acesso")
public class UserEntity {

  @Id
  @Column(nullable = false)
  @GeneratedValue(strategy = GenerationType.UUID)
  @Schema(description = "Identificador único do usuário", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
  private UUID id;

  @Column(nullable = false)
  @Schema(description = "Nome completo do usuário", example = "João Silva")
  private String name;

  @Column(nullable = false, unique = true)
  @Schema(description = "Endereço de e-mail único do usuário", example = "joao@email.com")
  private String email;

  @Column(nullable = false)
  @Schema(description = "Senha criptografada do usuário (Hash BCrypt)", example = "$2a$12$DUMMYHASH...")
  private String password;

  @Column(nullable = false)
  @Schema(description = "Saldo atual da carteira digital do usuário", example = "500.00")
  private BigDecimal balance;

  @Column(nullable = false)
  @Schema(description = "Indica se o usuário está ativo para realizar transações", example = "true")
  private boolean active;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  @Schema(description = "Data e hora de criação do registro", example = "2026-06-23T18:53:00Z")
  private Instant createdAt;
}