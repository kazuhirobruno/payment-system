package br.com.kazuhiro.payment_system.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import br.com.kazuhiro.payment_system.exceptions.dtos.ErrorResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Handler Global de Exceções - GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

  @InjectMocks
  private GlobalExceptionHandler globalExceptionHandler;

  @Test
  @DisplayName("1. Deve capturar MethodArgumentNotValidException e mapear os campos inválidos no ErrorResponse")
  void shouldHandleValidationExceptionsWithMultipleFields() {
    BindingResult bindingResult = mock(BindingResult.class);

    FieldError error1 = new FieldError("userDto", "email", "O e-mail informado é inválido.");
    FieldError error2 = new FieldError("userDto", "senha", "A senha deve conter no mínimo 6 caracteres.");

    when(bindingResult.getAllErrors()).thenReturn(List.of(error1, error2));

    MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
        mock(MethodParameter.class),
        bindingResult);

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(exception);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body, "O corpo da resposta não deveria ser nulo");

    assertNotNull(body.getTimestamp());
    assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
    assertEquals("Erro de validação nos campos enviados.", body.getError());

    Map<String, String> fieldsMap = body.getFields();
    assertNotNull(fieldsMap);
    assertEquals(2, fieldsMap.size());
    assertEquals("O e-mail informado é inválido.", fieldsMap.get("email"));
    assertEquals("A senha deve conter no mínimo 6 caracteres.", fieldsMap.get("senha"));
  }

  @Test
  @DisplayName("2. Deve tratar qualquer Exception genérica capturando a mensagem padrão no dicionário do ErrorResponse")
  void shouldHandleGlobalExceptionsAndReturnInternalServerError() {
    Exception exception = new RuntimeException("Falha de timeout na conexão com o banco de dados.");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalExceptions(exception);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    ErrorResponse body = response.getBody();
    assertNotNull(body, "O corpo da resposta não deveria ser nulo");

    assertNotNull(body.getTimestamp());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), body.getStatus());
    assertEquals("Ocorreu um erro interno no servidor.", body.getError());

    Map<String, String> fieldsMap = body.getFields();
    assertNotNull(fieldsMap);
    assertEquals(1, fieldsMap.size());
    assertEquals("Falha de timeout na conexão com o banco de dados.", fieldsMap.get("message"));
  }
}