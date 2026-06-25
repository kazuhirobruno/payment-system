package br.com.kazuhiro.payment_system.modules.transaction.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.transactions.controllers.TransactionController;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionAmountRequestDTO;
import br.com.kazuhiro.payment_system.modules.transactions.dtos.TransactionResponseDTO;
import br.com.kazuhiro.payment_system.modules.transactions.usecases.DepositUseCase;
import br.com.kazuhiro.payment_system.providers.UserJWTProvider;
import br.com.kazuhiro.payment_system.types.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
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
  private UserJWTProvider userJWTProvider;

  private String dummyUserId;
  private TransactionAmountRequestDTO requestDTO;
  private TransactionResponseDTO responseDTO;

  @BeforeEach
  void setUp() {
    dummyUserId = UUID.randomUUID().toString();

    requestDTO = new TransactionAmountRequestDTO(new BigDecimal("100.25"));

    responseDTO = TransactionResponseDTO.builder()
        .transactionId(UUID.randomUUID())
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.25"))
        .newBalance(new BigDecimal("600.25"))
        .createdAt(Instant.now())
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
}