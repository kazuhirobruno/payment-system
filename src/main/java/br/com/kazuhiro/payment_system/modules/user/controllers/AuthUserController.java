package br.com.kazuhiro.payment_system.modules.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.AuthUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "Autenticação", description = "Endpoints responsáveis pela segurança e acesso do usuário")
public class AuthUserController {

  private final AuthUserUseCase authUserUseCase;

  @PostMapping("/auth")
  @Operation(summary = "Autenticar usuário", description = "Valida as credenciais do usuário (e-mail e senha) e retorna um token JWT válido para acesso a rotas protegidas.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciais necessárias para a autenticação do usuário", required = true, content = @Content(schema = @Schema(implementation = AuthUserRequestDTO.class)))
  @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso.", content = @Content(schema = @Schema(implementation = AuthUserResponseDTO.class)))
  @ApiResponse(responseCode = "401", description = "Credenciais inválidas ou usuário não autorizado.", content = @Content(schema = @Schema(type = "string", example = "Email/senha incorreto.")))
  @ApiResponse(responseCode = "404", description = "Usuário não encontrado ou inativo.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  public ResponseEntity<Object> auth(@RequestBody AuthUserRequestDTO authUserRequestDTO) {
    try {
      var response = this.authUserUseCase.execute(authUserRequestDTO);
      return ResponseEntity.ok().body(response);
    } catch (DeletedUserLoginException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }
  }
}