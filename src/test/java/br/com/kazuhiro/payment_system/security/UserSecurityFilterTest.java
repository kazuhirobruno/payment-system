package br.com.kazuhiro.payment_system.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import br.com.kazuhiro.payment_system.providers.UserJWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Filtro de Segurança - UserSecurityFilter")
class UserSecurityFilterTest {

  @Mock
  private UserJWTProvider userJWTProvider;

  @Mock
  private FilterChain filterChain;

  @InjectMocks
  private UserSecurityFilter userSecurityFilter;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/products/list",
      "/user",
      "/user/auth///",
      "",
      "///"
  })
  @DisplayName("Deve seguir o fluxo sem exigir token para rotas não monitoradas, públicas ou vazias")
  void shouldProceedWithoutAuthenticationForPublicOrUnmonitoredEndpoints(String uri)
      throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(uri);
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/user/profile",
      "/transaction/checkout/",
      "/transaction/list///"
  })
  @DisplayName("Deve retornar 401 quando o cabeçalho 'Authorization' for nulo e forçar o loop de barras em rotas privadas")
  void shouldReturnUnauthorizedWhenHeaderIsNullOnMonitoredEndpoints(String uri) throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI(uri);
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertEquals("application/json;charset=UTF-8", response.getContentType());
    assertTrue(
        response.getContentAsString().contains("O token de autenticação é obrigatório para acessar este recurso."));
    verify(filterChain, never()).doFilter(any(), any());
  }

  @Test
  @DisplayName("Deve retornar 401 se o token resultante após o mapeamento do prefixo for exatamente 'Bearer'")
  void shouldReturnUnauthorizedWhenTokenPuroIsExactlyBearer() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/transaction/checkout");
    request.addHeader("Authorization", "Bearer Bearer");
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertTrue(response.getContentAsString().contains("O token JWT enviado está incompleto ou inválido."));
  }

  @Test
  @DisplayName("Deve retornar 401 sem corpo JSON se o provedor de JWT falhar e retornar um token nulo")
  void shouldReturnUnauthorizedWhenTokenValidationReturnsNull() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/user/profile");
    request.addHeader("Authorization", "Bearer invalid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(userJWTProvider.validateToken("invalid-token")).thenReturn(null);

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("Deve retornar 401 com mensagem descritiva se o token disparar a exceção 'TokenExpiredException'")
  void shouldReturnUnauthorizedWhenTokenIsExpired() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/user/profile");
    request.addHeader("Authorization", "Bearer expired-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    when(userJWTProvider.validateToken("expired-token")).thenThrow(new TokenExpiredException("Expired", null));

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
    assertTrue(response.getContentAsString().contains("O token enviado está expirado. Faça login novamente."));
  }

  @Test
  @DisplayName("Deve autenticar com sucesso no Spring Security quando o token for válido e possuir o prefixo 'Bearer '")
  void shouldAuthenticateSuccessfullyWithBearerToken() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/transaction/history");
    request.addHeader("Authorization", "Bearer valid-token");
    MockHttpServletResponse response = new MockHttpServletResponse();

    DecodedJWT decodedJWT = mock(DecodedJWT.class);
    Claim rolesClaim = mock(Claim.class);

    when(decodedJWT.getSubject()).thenReturn("userId123");
    when(decodedJWT.getClaim("roles")).thenReturn(rolesClaim);
    when(rolesClaim.asList(Object.class)).thenReturn(Collections.singletonList("admin"));
    when(userJWTProvider.validateToken("valid-token")).thenReturn(decodedJWT);

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assertEquals("userId123", request.getAttribute("user_id"));
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Deve extrair token sem o prefixo 'Bearer ' e autenticar com sucesso")
  void shouldAuthenticateSuccessfullyWhenTokenDoesNotHaveBearerPrefix() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/user/profile");
    request.addHeader("Authorization", "raw-token-without-bearer");
    MockHttpServletResponse response = new MockHttpServletResponse();

    DecodedJWT decodedJWT = mock(DecodedJWT.class);
    Claim rolesClaim = mock(Claim.class);

    when(decodedJWT.getSubject()).thenReturn("userId456");
    when(decodedJWT.getClaim("roles")).thenReturn(rolesClaim);
    when(rolesClaim.asList(Object.class)).thenReturn(Collections.singletonList("user"));
    when(userJWTProvider.validateToken("raw-token-without-bearer")).thenReturn(decodedJWT);

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Deve forçar a cobertura de end == 0 quando a rota monitorada for limpa pelo loop até zerar")
  void shouldCoverEndEqualsZeroInsidePublicRoute() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/user/../../../../");
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);
    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  @DisplayName("Deve forçar a cobertura de URI nula dentro do método isPublicRoute")
  void shouldCoverNullUriInsidePublicRoute() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest() {
      @Override
      public String getRequestURI() {
        return null;
      }
    };
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);

    verify(filterChain, times(1)).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve tratar com segurança e seguir o fluxo quando a URI da requisição for nula")
  void shouldHandleNullUriSafelyAndProceed() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest() {
      @Override
      public String getRequestURI() {
        return null;
      }
    };
    MockHttpServletResponse response = new MockHttpServletResponse();

    userSecurityFilter.doFilterInternal(request, response, filterChain);
    verify(filterChain, times(1)).doFilter(request, response);
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = { "///" })
  @DisplayName("Deve validar isoladamente que cenários nulos, vazios ou de barras isoladas não são rotas públicas")
  void shouldEvaluateSpecialUrisInsidePublicRouteMethod(String uri) {
    boolean result = userSecurityFilter.isPublicRoute(uri);
    assertFalse(result);
  }
}