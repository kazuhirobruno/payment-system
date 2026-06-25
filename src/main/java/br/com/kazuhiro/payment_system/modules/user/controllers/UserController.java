package br.com.kazuhiro.payment_system.modules.user.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.UserProfileResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.CreateUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.DeleteUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.GetProfileUseCase;
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
  private final DeleteUserUseCase deleteUserUseCase;
  private final GetProfileUseCase getProfileUseCase;

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

  @DeleteMapping("/delete")
  @Operation(summary = "Deletar usuário autenticado", description = "Remove a conta do usuário logado no sistema com base no ID extraído do token JWT.")
  @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso. Não retorna corpo na resposta.")
  @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido.", content = @Content(schema = @Schema(type = "string", example = "O token enviado está expirado. Faça login novamente.")))
  @ApiResponse(responseCode = "404", description = "Usuário não encontrado na base de dados (ex: conta já excluída).", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  @ApiResponse(responseCode = "500", description = "Erro interno no servidor ou falha de comunicação com o banco de dados.", content = @Content(schema = @Schema(type = "string", example = "Ocorreu um erro interno no servidor.")))
  public ResponseEntity<Void> delete(@RequestAttribute("user_id") String userId) {
    try {
      this.deleteUserUseCase.delete(userId);
      return ResponseEntity.noContent().build();
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/profile")
  @Operation(summary = "Obter dados do perfil", description = "Recupera as informações cadastrais e o saldo atual da carteira digital do usuário autenticado.")
  @ApiResponse(responseCode = "200", description = "Dados do perfil retornados com sucesso.", content = @Content(schema = @Schema(implementation = UserProfileResponseDTO.class)))
  @ApiResponse(responseCode = "401", description = "Token ausente, expirado ou inválido.", content = @Content(schema = @Schema(type = "string", example = "Token de autenticação inválido ou malformado.")))
  @ApiResponse(responseCode = "404", description = "Usuário não encontrado na base de dados.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  @ApiResponse(responseCode = "500", description = "Erro interno no servidor.", content = @Content(schema = @Schema(type = "string", example = "Ocorreu um erro interno no servidor.")))
  public ResponseEntity<Object> balance(@RequestAttribute("user_id") String userId) {
    try {
      var response = this.getProfileUseCase.execute(userId);
      return ResponseEntity.ok().body(response);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
