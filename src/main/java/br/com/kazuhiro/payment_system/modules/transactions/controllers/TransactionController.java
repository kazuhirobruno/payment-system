package br.com.kazuhiro.payment_system.modules.transactions.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.NegativeAmountException;
import br.com.kazuhiro.payment_system.exceptions.ReceiverUserInactiveException;
import br.com.kazuhiro.payment_system.exceptions.SameAccountTransferException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.StatementItemResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransferResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.DepositUseCase;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.GetStatementUseCase;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.TransferUseCase;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.WithdrawUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
@Tag(name = "Transações", description = "Endpoints responsáveis pelas movimentações financeiras dos usuários")
public class TransactionController {
  private final DepositUseCase depositUseCase;
  private final WithdrawUseCase withdrawUseCase;
  private final TransferUseCase transferUseCase;
  private final GetStatementUseCase getStatementUseCase;

  @PostMapping("/deposit")
  @Operation(summary = "Realizar um depósito", description = "Adiciona um valor monetário ao saldo da carteira do usuário autenticado no sistema.")
  @SecurityRequirement(name = "bearer-key")
  @ApiResponse(responseCode = "201", description = "Depósito realizado com sucesso. Retorna o comprovante da operação.", content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class)))
  @ApiResponse(responseCode = "404", description = "Usuário destino não foi encontrado no sistema.", content = @Content(schema = @Schema(type = "string", example = "Erro na operação solicitada.")))
  @ApiResponse(responseCode = "403", description = "Operação recusada. O usuário está inativo/deletado no sistema.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  public ResponseEntity<Object> deposit(@RequestAttribute("user_id") String userId,
      @Valid @RequestBody TransactionAmountRequestDTO transactionAmountRequestDTO) {
    try {
      var response = this.depositUseCase.execute(transactionAmountRequestDTO, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (DeletedUserLoginException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

  @PostMapping("/withdraw")
  @Operation(summary = "Realizar um saque", description = "Deduz um valor monetário do saldo da carteira do usuário autenticado no sistema.")
  @SecurityRequirement(name = "bearer-key")
  @ApiResponse(responseCode = "201", description = "Saque realizado com sucesso. Retorna o comprovante da operação.", content = @Content(schema = @Schema(implementation = TransactionResponseDTO.class)))
  @ApiResponse(responseCode = "400", description = "O valor enviado viola as validações de formato.", content = @Content)
  @ApiResponse(responseCode = "404", description = "Usuário não foi encontrado no sistema.", content = @Content(schema = @Schema(type = "string", example = "Erro na operação solicitada.")))
  @ApiResponse(responseCode = "403", description = "Operação recusada. O usuário está inativo/deletado no sistema.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  @ApiResponse(responseCode = "422", description = "Operação recusada. Saldo insuficiente para concluir o saque.", content = @Content(schema = @Schema(type = "string", example = "Saldo insuficiente.")))
  public ResponseEntity<Object> withdraw(@RequestAttribute("user_id") String userId,
      @Valid @RequestBody TransactionAmountRequestDTO transactionAmountRequestDTO) {
    try {
      var response = this.withdrawUseCase.execute(transactionAmountRequestDTO, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (DeletedUserLoginException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (NegativeAmountException e) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
    }
  }

  @PostMapping("/transfer")
  @Operation(summary = "Realizar uma transferência", description = "Transfere um valor monetário da conta do usuário autenticado para outro usuário cadastrado.")
  @SecurityRequirement(name = "bearer-key")
  @ApiResponse(responseCode = "201", description = "Transferência concluída com sucesso. Retorna o comprovante.", content = @Content(schema = @Schema(implementation = TransferResponseDTO.class)))
  @ApiResponse(responseCode = "404", description = "O usuário remetente ou o destinatário não foi encontrado.", content = @Content(schema = @Schema(type = "string", example = "Erro na operação solicitada.")))
  @ApiResponse(responseCode = "403", description = "A operação foi recusada porque um dos usuários envolvidos está inativo.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  @ApiResponse(responseCode = "422", description = "Recusado por saldo insuficiente ou tentativa de auto-transferência.", content = @Content(schema = @Schema(type = "string", example = "Saldo insuficiente.")))
  public ResponseEntity<Object> transfer(@RequestAttribute("user_id") String userId,
      @Valid @RequestBody TransferRequestDTO transferRequestDTO) {
    try {
      var response = this.transferUseCase.execute(transferRequestDTO, userId);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (DeletedUserLoginException | ReceiverUserInactiveException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    } catch (NegativeAmountException | SameAccountTransferException e) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
    }
  }

  @GetMapping("/statement")
  @Operation(summary = "Consultar extrato financeiro do usuário autenticado", description = "Gera o histórico de movimentações (depósitos, saques e transferências) associadas à conta do cliente logado de forma paginada.")
  @SecurityRequirement(name = "bearer-key")
  @ApiResponse(responseCode = "200", description = "Extrato retornado com sucesso em conformidade com o padrão de paginação REST.", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = StatementItemResponseDTO.class))))
  @ApiResponse(responseCode = "401", description = "Acesso negado. Token de autenticação ausente, expirado ou inválido.", content = @Content(schema = @Schema(type = "string", example = "Token de autenticação inválido ou malformado.")))
  @ApiResponse(responseCode = "404", description = "Recurso não localizado. Usuário autenticado não existe na base de dados.", content = @Content(schema = @Schema(type = "string", example = "Erro na operação solicitada.")))
  @ApiResponse(responseCode = "403", description = "Operação proibida. O usuário associado ao extrato está com a conta inativa.", content = @Content(schema = @Schema(type = "string", example = "Usuário não encontrado.")))
  public ResponseEntity<Object> getStatement(
      @RequestAttribute("user_id") String userId,
      Pageable pageable) {
    try {
      var response = this.getStatementUseCase.execute(userId, pageable);
      return ResponseEntity.ok(response);
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    } catch (DeletedUserLoginException e) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }
}
