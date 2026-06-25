package br.com.kazuhiro.payment_system.modules.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.exceptions.UserNotFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.ChangePasswordRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.UserProfileResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.ChangePasswordUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.CreateUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.DeleteUserUseCase;
import br.com.kazuhiro.payment_system.modules.user.usecases.GetProfileUseCase;
import br.com.kazuhiro.payment_system.providers.UserJWTProvider;

import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
  private ChangePasswordUseCase changePasswordUseCase;

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
  void shouldCreateUserWithSuccess() throws Exception {
    var responseDTO = CreateUserResponseDTO.builder()
        .id(UUID.fromString(sampleUserId))
        .name("John Doe")
        .email("john.doe@example.com")
        .build();

    when(createUserUseCase.execute(any(CreateUserRequestDTO.class))).thenReturn(responseDTO);

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(sampleUserId))
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"));
  }

  @Test
  void shouldReturnBadRequestWhenPasswordsDoNotMatch() throws Exception {
    when(createUserUseCase.execute(any(CreateUserRequestDTO.class)))
        .thenThrow(new PasswordNotMatchesException());

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("As senhas não coincidem."));
  }

  @Test
  void shouldReturnConflictWhenUserEmailAlreadyExists() throws Exception {
    when(createUserUseCase.execute(any(CreateUserRequestDTO.class)))
        .thenThrow(new UserFoundException());

    mockMvc.perform(post("/user/")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(validUserRequestDTO)))
        .andExpect(status().isConflict())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  void shouldDeleteUserWithSuccess() throws Exception {
    doNothing().when(deleteUserUseCase).delete(sampleUserId);

    mockMvc.perform(delete("/user/delete")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundWhenDeletingNonExistingUser() throws Exception {
    doThrow(new UserNotFoundException()).when(deleteUserUseCase).delete(sampleUserId);

    mockMvc.perform(delete("/user/delete")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  void shouldGetProfileWithSuccess() throws Exception {
    var profileResponse = UserProfileResponseDTO.builder()
        .name("John Doe")
        .email("john.doe@example.com")
        .balance(new BigDecimal("100.00"))
        .build();

    when(getProfileUseCase.execute(sampleUserId)).thenReturn(profileResponse);

    mockMvc.perform(get("/user/profile")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("John Doe"))
        .andExpect(jsonPath("$.email").value("john.doe@example.com"))
        .andExpect(jsonPath("$.balance").value(100.00));
  }

  @Test
  void shouldReturnNotFoundWhenGettingProfileOfNonExistingUser() throws Exception {
    when(getProfileUseCase.execute(sampleUserId))
        .thenThrow(new UserNotFoundException());

    mockMvc.perform(get("/user/profile")
        .requestAttr("user_id", sampleUserId))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  void shouldChangePasswordWithSuccess() throws Exception {
    var changePasswordRequestDTO = ChangePasswordRequestDTO.builder()
        .password("NewPassword123")
        .confirmPassword("NewPassword123")
        .build();
    doNothing().when(changePasswordUseCase).execute(any(ChangePasswordRequestDTO.class), eq(sampleUserId));

    mockMvc.perform(patch("/user/password")
        .requestAttr("user_id", sampleUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(changePasswordRequestDTO)))
        .andExpect(status().isNoContent());
  }

  @Test
  void shouldReturnNotFoundWhenChangingPasswordOfNonExistingUser() throws Exception {
    var changePasswordRequestDTO = ChangePasswordRequestDTO.builder()
        .password("NewPassword123")
        .confirmPassword("NewPassword123")
        .build();

    doThrow(new UserNotFoundException())
        .when(changePasswordUseCase)
        .execute(any(ChangePasswordRequestDTO.class), eq(sampleUserId));

    mockMvc.perform(patch("/user/password")
        .requestAttr("user_id", sampleUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(changePasswordRequestDTO)))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Erro na operação solicitada."));
  }

  @Test
  void shouldReturnBadRequestWhenNewPasswordsDoNotMatch() throws Exception {
    var changePasswordRequestDTO = ChangePasswordRequestDTO.builder()
        .password("NewPassword123")
        .confirmPassword("WrongPassword123")
        .build();
    doThrow(new PasswordNotMatchesException())
        .when(changePasswordUseCase)
        .execute(any(ChangePasswordRequestDTO.class), eq(sampleUserId));

    mockMvc.perform(patch("/user/password")
        .requestAttr("user_id", sampleUserId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(changePasswordRequestDTO)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("As senhas não coincidem."));
  }
}