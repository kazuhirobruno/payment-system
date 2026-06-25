package br.com.kazuhiro.payment_system.modules.transactions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.kazuhiro.payment_system.modules.transactions.entities.TransactionEntity;
import br.com.kazuhiro.payment_system.modules.transactions.repository.TransactionRepository;
import br.com.kazuhiro.payment_system.modules.user.entities.UserEntity;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import br.com.kazuhiro.payment_system.types.TransactionType;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class TransactionStatementIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TransactionRepository transactionRepository;

  private UserEntity mainUser;
  private UserEntity secondaryUser;

  @BeforeEach
  void setUp() {
    mainUser = UserEntity.builder()
        .name("John Doe")
        .email("john.doe@example.com")
        .password("secure_password")
        .balance(new BigDecimal("500.00"))
        .active(true)
        .createdAt(Instant.now())
        .build();

    secondaryUser = UserEntity.builder()
        .name("Jane Doe")
        .email("jane.doe@example.com")
        .password("secure_password")
        .balance(new BigDecimal("200.00"))
        .active(true)
        .createdAt(Instant.now())
        .build();

    mainUser = userRepository.save(mainUser);
    secondaryUser = userRepository.save(secondaryUser);
  }

  @Test
  @DisplayName("Fluxo Integrado: Deve retornar o extrato paginado lendo dados reais do H2")
  void shouldReturnStatementFromDatabaseWithSuccess() throws Exception {
    TransactionEntity deposit = TransactionEntity.builder()
        .type(TransactionType.DEPOSIT)
        .amount(new BigDecimal("100.00"))
        .sender(null)
        .receiver(mainUser)
        .createdAt(Instant.now())
        .build();

    TransactionEntity transfer = TransactionEntity.builder()
        .type(TransactionType.TRANSFER)
        .amount(new BigDecimal("50.00"))
        .sender(mainUser)
        .receiver(secondaryUser)
        .createdAt(Instant.now().plusSeconds(10))
        .build();

    transactionRepository.save(deposit);
    transactionRepository.save(transfer);

    mockMvc.perform(get("/transaction/statement")
        .requestAttr("user_id", mainUser.getId().toString())
        .param("page", "0")
        .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.content[0].type").value("TRANSFER"))
        .andExpect(jsonPath("$.content[0].counterpartName").value("Jane Doe"))
        .andExpect(jsonPath("$.content[1].type").value("DEPOSIT"))
        .andExpect(jsonPath("$.content[1].counterpartName").value((Object) null));
  }

  @Test
  @DisplayName("Fluxo Integrado: Deve retornar 404 Not Found se o usuário do request não existir no H2")
  void shouldReturnNotFoundWhenUserDoesNotExistInDatabase() throws Exception {
    String nonExistingUserId = UUID.randomUUID().toString();

    mockMvc.perform(get("/transaction/statement")
        .requestAttr("user_id", nonExistingUserId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$").value("Erro na operação solicitada."));
  }
}