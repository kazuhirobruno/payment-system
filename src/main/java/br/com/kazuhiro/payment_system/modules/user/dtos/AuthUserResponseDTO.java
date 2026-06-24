package br.com.kazuhiro.payment_system.modules.user.dtos;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Objeto de resposta retornado após uma autenticação bem-sucedida")
public class AuthUserResponseDTO {

  @Schema(description = "Token de acesso no formato JWT (JSON Web Token)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String token;

  @Schema(description = "Data e hora exata em que o token irá expirar (padrão UTC)", example = "2026-06-24T15:52:00Z")
  private Instant expiresAt;

  @Schema(description = "Lista de permissões/perfis atribuídos ao usuário autenticado", example = "[\"ADMIN\", \"USER\"]")
  private List<String> roles;
}