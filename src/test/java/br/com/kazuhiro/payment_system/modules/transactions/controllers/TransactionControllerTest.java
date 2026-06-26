package br.com.kazuhiro.payment_system.modules.transactions.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import br.com.kazuhiro.payment_system.providers.UserJWTProvider;
import br.com.kazuhiro.payment_system.types.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private DepositUseCase depositUseCase;

  @MockitoBean
  private WithdrawUseCase withdrawUseCase;

  @MockitoBean
  private UserJWTProvider userJWTProvider;

  @MockitoBean
  private TransferUseCase transferUseCase;

  @MockitoBean
  private GetStatementUseCase getStatementUseCase;

  private String dummyUserId;
  private TransactionAmountRequestDTO requestDTO;
  private TransactionResponseDTO responseDTO;
  private Instant fixedTime = Instant.parse("2026-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    dummyUserId = UUID.randomUUID().toString();

    requestDTO = new TransactionAmountRequestDTO(new BigDecimal("100.25"));

    responseDTO = TransactionResponseDTO.builder()
        .transactionId(UUID.randomUUID())
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.25"))
        .newBalance(new BigDecimal("600.25"))
        .createdAt(fixedTime)
        .build();
  }

  @Test
  @DisplayName("Deve realizar depósito com sucesso e retornar 201 Created")
  void shouldDepositWithSuccess() throws Exception {
    when(depositUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenReturn(responseDTO);

    mockMvc.perform(post("/transaction/deposit")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("DEPOSIT"))
        .andExpect(jsonPath("$.amount").value(100.25))
        .andExpect(jsonPath("$.newBalance").value(600.25));
  }

  @Test
  @DisplayName("Deve retornar 404 Not Found quando o usuário não existir")
  void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
    when(depositUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new UserNotFoundException());

    mockMvc.perform(post("/transaction/deposit")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Deve retornar 403 Forbidden quando o usuário estiver inativo/deletado")
  void shouldReturnForbiddenWhenUserIsDeleted() throws Exception {
    when(depositUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new DeletedUserLoginException());

    mockMvc.perform(post("/transaction/deposit")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("Deve barrar e retornar 400 Bad Request se o valor enviado for negativo")
  void shouldReturnBadRequestWhenAmountIsNegative() throws Exception {
    TransactionAmountRequestDTO invalidRequest = new TransactionAmountRequestDTO(new BigDecimal("-50.00"));

    mockMvc.perform(post("/transaction/deposit")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve realizar saque com sucesso e retornar 201 Created")
  void shouldWithdrawWithSuccess() throws Exception {
    var withdrawResponse = TransactionResponseDTO.builder()
        .transactionId(UUID.randomUUID())
        .type(TransactionType.WITHDRAW)
        .amount(new BigDecimal("50.00"))
        .newBalance(new BigDecimal("450.00"))
        .createdAt(fixedTime)
        .build();

    when(withdrawUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenReturn(withdrawResponse);

    mockMvc.perform(post("/transaction/withdraw")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("WITHDRAW"))
        .andExpect(jsonPath("$.amount").value(50.00))
        .andExpect(jsonPath("$.newBalance").value(450.00));
  }

  @Test
  @DisplayName("Deve retornar 404 Not Found no saque quando o usuário não existir")
  void shouldReturnNotFoundOnWithdrawWhenUserDoesNotExist() throws Exception {
    when(withdrawUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new UserNotFoundException());

    mockMvc.perform(post("/transaction/withdraw")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  @DisplayName("Deve retornar 403 Forbidden no saque quando o usuário estiver inativo")
  void shouldReturnForbiddenOnWithdrawWhenUserIsDeleted() throws Exception {
    when(withdrawUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new DeletedUserLoginException());

    mockMvc.perform(post("/transaction/withdraw")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Usuário não encontrado."));
  }

  @Test
  @DisplayName("Deve retornar 422 Unprocessable Entity quando o saldo for insuficiente")
  void shouldReturnUnprocessableEntityWhenBalanceIsInsufficient() throws Exception {
    when(withdrawUseCase.execute(any(TransactionAmountRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new NegativeAmountException());

    mockMvc.perform(post("/transaction/withdraw")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string("Saldo insuficiente."));
  }

  @Test
  @DisplayName("Deve barrar e retornar 400 Bad Request no saque se o valor for negativo")
  void shouldReturnBadRequestOnWithdrawWhenAmountIsNegative() throws Exception {
    TransactionAmountRequestDTO invalidRequest = new TransactionAmountRequestDTO(new BigDecimal("-20.00"));

    mockMvc.perform(post("/transaction/withdraw")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve realizar transferência com sucesso e retornar 201 Created")
  void shouldTransferWithSuccess() throws Exception {
    var transferRequest = TransferRequestDTO.builder()
        .receiverId(UUID.randomUUID())
        .amount(new BigDecimal("50.00"))
        .build();

    var transferResponse = TransferResponseDTO.builder()
        .transactionId(UUID.randomUUID())
        .type(TransactionType.TRANSFER.name())
        .amount(new BigDecimal("50.00"))
        .newBalance(new BigDecimal("400.00"))
        .receiverId(transferRequest.getReceiverId())
        .receiverName("Jane Doe")
        .createdAt(fixedTime)
        .build();

    when(transferUseCase.execute(any(TransferRequestDTO.class), eq(dummyUserId)))
        .thenReturn(transferResponse);

    mockMvc.perform(post("/transaction/transfer")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("TRANSFER"))
        .andExpect(jsonPath("$.amount").value(50.00))
        .andExpect(jsonPath("$.newBalance").value(400.00))
        .andExpect(jsonPath("$.receiverName").value("Jane Doe"));
  }

  @Test
  @DisplayName("Deve retornar 404 Not Found quando uma das contas não for localizada")
  void shouldReturnNotFoundOnTransferWhenUserDoesNotExist() throws Exception {
    var transferRequest = TransferRequestDTO.builder()
        .receiverId(UUID.randomUUID())
        .amount(new BigDecimal("10.00"))
        .build();

    when(transferUseCase.execute(any(TransferRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new UserNotFoundException());

    mockMvc.perform(post("/transaction/transfer")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferRequest)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  @DisplayName("Deve retornar 403 Forbidden quando o remetente ou destinatário estiver inativo")
  void shouldReturnForbiddenOnTransferWhenUserIsInactive() throws Exception {
    var transferRequest = TransferRequestDTO.builder()
        .receiverId(UUID.randomUUID())
        .amount(new BigDecimal("10.00"))
        .build();

    when(transferUseCase.execute(any(TransferRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new ReceiverUserInactiveException());

    mockMvc.perform(post("/transaction/transfer")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferRequest)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Usuário não encontrado."));
  }

  @Test
  @DisplayName("Deve retornar 422 Unprocessable Entity quando houver quebra de regra de negócio")
  void shouldReturnUnprocessableEntityOnTransferWhenBusinessRuleFails() throws Exception {
    var transferRequest = TransferRequestDTO.builder()
        .receiverId(UUID.randomUUID())
        .amount(new BigDecimal("500.00"))
        .build();

    when(transferUseCase.execute(any(TransferRequestDTO.class), eq(dummyUserId)))
        .thenThrow(new SameAccountTransferException());

    mockMvc.perform(post("/transaction/transfer")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(transferRequest)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(content().string("Vetada a transferência para o mesmo usuário."));
  }

  @Test
  @DisplayName("Deve barrar e retornar 400 Bad Request se a transferência tiver payload malformado")
  void shouldReturnBadRequestOnTransferWhenPayloadIsInvalid() throws Exception {
    var invalidRequest = TransferRequestDTO.builder()
        .receiverId(null)
        .amount(new BigDecimal("-5.00"))
        .build();

    mockMvc.perform(post("/transaction/transfer")
        .requestAttr("user_id", dummyUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Deve retornar 200 OK com o extrato paginado do usuário com sucesso")
  void shouldReturnStatementWithSuccess() throws Exception {
    var item = StatementItemResponseDTO.builder()
        .transactionId(UUID.randomUUID())
        .type("DEPOSIT")
        .amount(new BigDecimal("100.00"))
        .createdAt(fixedTime)
        .counterpartName(null)
        .build();

    org.springframework.data.domain.Page<StatementItemResponseDTO> pageResponse = new org.springframework.data.domain.PageImpl<>(
        List.of(item));

    when(getStatementUseCase.execute(eq(dummyUserId), any(org.springframework.data.domain.Pageable.class)))
        .thenReturn(pageResponse);

    mockMvc.perform(get("/transaction/statement")
        .requestAttr("user_id", dummyUserId)
        .param("page", "0")
        .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.content[0].amount").value(100.00))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("Deve retornar 404 Not Found no extrato quando o usuário não for localizado")
  void shouldReturnNotFoundOnStatementWhenUserDoesNotExist() throws Exception {
    when(getStatementUseCase.execute(eq(dummyUserId), any(org.springframework.data.domain.Pageable.class)))
        .thenThrow(new UserNotFoundException());

    mockMvc.perform(get("/transaction/statement")
        .requestAttr("user_id", dummyUserId))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  @DisplayName("Deve retornar 403 Forbidden no extrato quando o usuário estiver com a conta inativa")
  void shouldReturnForbiddenOnStatementWhenUserIsDeleted() throws Exception {
    when(getStatementUseCase.execute(eq(dummyUserId), any(org.springframework.data.domain.Pageable.class)))
        .thenThrow(new DeletedUserLoginException());

    mockMvc.perform(get("/transaction/statement")
        .requestAttr("user_id", dummyUserId))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Usuário não encontrado."));
  }

}