package br.com.kazuhiro.payment_system.modules.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.CreateUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final CreateUserUseCase createUserUseCase;

  @PostMapping("/")
  @Operation(summary = "Cadastrar um novo usuário no sistema", description = "Esta rota é pública e permite a abertura de uma carteira digital com depósito de saldo inicial.")
  @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateUserResponseDTO.class)))
  @ApiResponse(responseCode = "400", description = "Dados enviados são inválidos ou as senhas não coincidem", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "As senhas não coincidem.")))
  @ApiResponse(responseCode = "409", description = "O endereço de e-mail enviado já está cadastrado no banco de dados", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Erro na operação solicitada.")))
  @ApiResponse(responseCode = "500", description = "Erro interno no servidor ao processar o cadastro", content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Erro interno ao processar o cadastro.")))
  public ResponseEntity<Object> create(@Valid @RequestBody CreateUserRequestDTO createUserRequestDTO) {
    try {
      var response = this.createUserUseCase.execute(createUserRequestDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (PasswordNotMatchesException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (UserFoundException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno ao processar o cadastro.");
    }
  }
}
