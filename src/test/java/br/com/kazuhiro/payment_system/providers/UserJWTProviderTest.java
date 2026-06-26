package br.com.kazuhiro.payment_system.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

@ExtendWith(MockitoExtension.class)
class UserJWTProviderTest {

  @InjectMocks
  private UserJWTProvider userJWTProvider;

  @BeforeEach
  void setSecret() {
    ReflectionTestUtils.setField(userJWTProvider, "secretKey", "my-secret");
  }

  @Test
  @DisplayName("Deve validar token com sucesso")
  void shouldValidateTokenSuccessfully() {

    String token = JWT.create()
        .sign(Algorithm.HMAC256("my-secret"));

    String bearerToken = "Bearer " + token;

    DecodedJWT result = userJWTProvider.validateToken(bearerToken);

    assertNotNull(result);
    assertEquals(result.getAlgorithm(), "HS256");
  }

  @Test
  @DisplayName("Deve retornar null quando assinatura for inválida")
  void shouldReturnNullWhenSignatureInvalid() {
    String token = JWT.create()
        .sign(Algorithm.HMAC256("wrong-secret"));
    String bearer = "Bearer " + token;
    DecodedJWT result = userJWTProvider.validateToken(bearer);
    assertNull(result);
  }

  @Test
  @DisplayName("Deve lançar TokenExpiredException quando token expirado")
  void shouldThrowTokenExpiredException() {

    String token = JWT.create()
        .withExpiresAt(new Date(System.currentTimeMillis() - 1000)) // expirado
        .sign(Algorithm.HMAC256("my-secret"));

    String bearerToken = "Bearer " + token;

    assertThrows(TokenExpiredException.class,
        () -> userJWTProvider.validateToken(bearerToken));
  }
}
