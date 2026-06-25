package br.com.kazuhiro.payment_system.modules.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.UserProfileResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.CreateUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.DeleteUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.GetProfileUseCase;
import br.com.kazuhiro.payment_system.providers.UserJWTProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private CreateUserUseCase createUserUseCase;

  @MockitoBean
  private DeleteUserUseCase deleteUserUseCase;

  @MockitoBean
  private GetProfileUseCase getProfileUseCase;

  @MockitoBean
  private UserJWTProvider userJWTProvider;

  private CreateUserRequestDTO validUserRequestDTO;
  private String sampleUserId;

  @BeforeEach
  void setUp() {
    this.sampleUserId = UUID.randomUUID().toString();
    this.validUserRequestDTO = CreateUserRequestDTO.builder()
        .name("John Doe")
        .email("john.doe@example.com")
        .password("SecurePassword123")
        .confirmPassword("SecurePassword123")
        .balance(new BigDecimal("100.00"))
        .build();
  }

  @Test
  @DisplayName("Should return 201 Created and user data when request body is valid")
  void shouldReturnCreatedWhenRequestBodyIsValid() throws Exception {
    CreateUserResponseDTO expectedUserResponseDTO = CreateUserResponseDTO.builder()
        .id(UUID.randomUUID())
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .build();

    when(createUserUseCase.execute(any(CreateUserRequestDTO.class))).thenReturn(expectedUserResponseDTO);

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"));

    verify(createUserUseCase, times(1)).execute(any(CreateUserRequestDTO.class));
  }

  @Test
  @DisplayName("Should return 400 Bad Request when password confirmation does not match")
  void shouldReturnBadRequestWhenPasswordConfirmationDoesNotMatch() throws Exception {
    String expectedErrorMessage = "As senhas não coincidem.";

    when(createUserUseCase.execute(any(CreateUserRequestDTO.class)))
        .thenThrow(new PasswordNotMatchesException());

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(expectedErrorMessage));

    verify(createUserUseCase, times(1)).execute(any(CreateUserRequestDTO.class));
  }

  @Test
  @DisplayName("Should return 409 Conflict when email address is already registered")
  void shouldReturnConflictWhenEmailIsAlreadyRegistered() throws Exception {
    String expectedErrorMessage = "Erro na operação solicitada.";

    when(createUserUseCase.execute(any(CreateUserRequestDTO.class)))
        .thenThrow(new UserFoundException());

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isConflict())
        .andExpect(content().string(expectedErrorMessage));

    verify(createUserUseCase, times(1)).execute(any(CreateUserRequestDTO.class));
  }

  @Test
  @DisplayName("Should return 500 Internal Server Error when unexpected exception occurs during creation")
  void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccursDuringCreation() throws Exception {
    when(createUserUseCase.execute(any(CreateUserRequestDTO.class)))
        .thenThrow(new RuntimeException("Database connection failure"));

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Erro interno ao processar o cadastro."));

    verify(createUserUseCase, times(1)).execute(any(CreateUserRequestDTO.class));
  }

  @Test
  @DisplayName("Should return 204 No Content when user account is successfully deleted")
  void shouldReturnNoContentWhenUserAccountIsSuccessfullyDeleted() throws Exception {
    doNothing().when(deleteUserUseCase).delete(sampleUserId);

    mockMvc.perform(delete("/user/delete")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNoContent());

    verify(deleteUserUseCase, times(1)).delete(sampleUserId);
  }

  @Test
  @DisplayName("Should return 404 Not Found when trying to delete a non-existing user identifier")
  void shouldReturnNotFoundWhenTryingToDeleteNonExistingUserIdentifier() throws Exception {
    doThrow(new UserNotFoundException()).when(deleteUserUseCase).delete(sampleUserId);

    mockMvc.perform(delete("/user/delete")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNotFound());

    verify(deleteUserUseCase, times(1)).delete(sampleUserId);
  }

  @Test
  @DisplayName("Should return 200 OK and profile data when user identifier is found")
  void shouldReturnOkAndProfileDataWhenUserIdentifierIsFound() throws Exception {
    UserProfileResponseDTO expectedProfileResponseDTO = UserProfileResponseDTO.builder()
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("350.75"))
        .build();

    when(getProfileUseCase.execute(sampleUserId)).thenReturn(expectedProfileResponseDTO);

    mockMvc.perform(get("/user/profile")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.balance").value(350.75));

    verify(getProfileUseCase, times(1)).execute(sampleUserId);
  }

  @Test
  @DisplayName("Should return 404 Not Found when profile user identifier does not exist")
  void shouldReturnNotFoundWhenProfileUserIdentifierDoesNotExist() throws Exception {
    when(getProfileUseCase.execute(sampleUserId)).thenThrow(new UserNotFoundException());

    mockMvc.perform(get("/user/profile")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNotFound());

    verify(getProfileUseCase, times(1)).execute(sampleUserId);
  }

  @Test
  @DisplayName("Should return 500 Internal Server Error when unexpected failure occurs during profile retrieval")
  void shouldReturnInternalServerErrorWhenUnexpectedFailureOccursDuringProfileRetrieval() throws Exception {
    when(getProfileUseCase.execute(sampleUserId)).thenThrow(new RuntimeException("Database timeout failure"));

    mockMvc.perform(get("/user/profile")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isInternalServerError());

    verify(getProfileUseCase, times(1)).execute(sampleUserId);
  }
}