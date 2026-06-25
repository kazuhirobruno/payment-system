package br.com.kazuhiro.payment_system.modules.transactions.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.NegativeAmountException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.DepositUseCase;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.WithdrawUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
@Tag(name = "Transações", description = "Endpoints responsáveis pelas movimentações financeiras dos usuários")
public class TransactionController {
  private final DepositUseCase depositUseCase;
  private final WithdrawUseCase withdrawUseCase;

  @PostMapping("/deposit")
  @Operation(summary = "Realizar um depósito", description = "Adiciona um valor monetário ao saldo da carteira do usuário autenticado no sistema.")
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
}
