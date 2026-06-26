package br.com.kazuhiro.payment_system.modules.user.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.AuthUserUseCase;
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

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthUserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthUserUseCase authUserUseCase;

  @MockitoBean
  private UserJWTProvider userJWTProvider;

  private AuthUserRequestDTO requestDTO;

  private Instant fixedTime = Instant.parse("2076-01-01T00:00:00Z");

  @BeforeEach
  void setUp() {
    this.requestDTO = AuthUserRequestDTO.builder()
        .email("user@email.com")
        .password("password123")
        .build();
  }

  @Test
  @DisplayName("Deve retornar 200 OK e o token quando as credenciais forem válidas")
  void shouldReturnOkWhenCredentialsAreValid() throws Exception {
    AuthUserResponseDTO expectedResponse = AuthUserResponseDTO.builder()
        .token("mocked-jwt-token")
        .expiresAt(fixedTime.plusSeconds(3600))
        .roles(List.of("USER"))
        .build();

    when(authUserUseCase.execute(any(AuthUserRequestDTO.class))).thenReturn(expectedResponse);

    mockMvc.perform(post("/user/auth")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("mocked-jwt-token"))
        .andExpect(jsonPath("$.roles[0]").value("USER"))
        .andExpect(jsonPath("$.expiresAt").exists());

    verify(authUserUseCase, times(1)).execute(any(AuthUserRequestDTO.class));
  }

  @Test
  @DisplayName("Should return 404 Not Found when user account is inactive or deleted")
  void shouldReturnNotFoundWhenUserAccountIsInactiveOrDeleted() throws Exception {
    String expectedErrorMessage = "Usuário não encontrado.";

    when(authUserUseCase.execute(any(AuthUserRequestDTO.class)))
        .thenThrow(new DeletedUserLoginException());

    mockMvc.perform(post("/user/auth")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))

        .andExpect(status().isNotFound())
        .andExpect(content().string(expectedErrorMessage));

    verify(authUserUseCase, times(1)).execute(any(AuthUserRequestDTO.class));
  }

  @Test
  @DisplayName("Deve retornar 401 Unauthorized quando ocorrer qualquer outra falha de autenticação")
  void shouldReturnUnauthorizedWhenAuthenticationFails() throws Exception {
    String errorMessage = "Email/senha incorreto.";
    when(authUserUseCase.execute(any(AuthUserRequestDTO.class)))
        .thenThrow(new RuntimeException(errorMessage));

    mockMvc.perform(post("/user/auth")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(requestDTO)))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(errorMessage));

    verify(authUserUseCase, times(1)).execute(any(AuthUserRequestDTO.class));
  }
}