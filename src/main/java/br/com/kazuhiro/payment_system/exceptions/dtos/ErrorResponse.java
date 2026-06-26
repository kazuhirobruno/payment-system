package br.com.kazuhiro.payment_system.exceptions.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

@Data
@Schema(description = "Estrutura padrão de resposta para erros da API")
public class ErrorResponse {

  @Schema(description = "Data e hora do erro", example = "2026-06-23T12:05:00")
  private Instant timestamp;

  @Schema(description = "Código do status HTTP", example = "400")
  private int status;

  @Schema(description = "Título descritivo do erro", example = "Erro de Validação")
  private String error;

  @Schema(description = "Dicionário contendo os campos inválidos e seus respectivos motivos")
  private Map<String, String> fields;

  public ErrorResponse(int status, String error, Map<String, String> fields) {
    this.timestamp = Instant.now(Clock.systemUTC());
    this.status = status;
    this.error = error;
    this.fields = fields;
  }
}