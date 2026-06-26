package br.com.kazuhiro.payment_system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.kazuhiro.payment_system.providers.UserJWTProvider;

@ExtendWith(MockitoExtension.class)
class UserSecurityFilterTest {

  @Mock
  private UserJWTProvider userJWTProvider;

  @Mock
  private FilterChain filterChain;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @InjectMocks
  private UserSecurityFilter filter;

  @BeforeEach
  void setup() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Deve liberar acesso para endpoints públicos (/user e /user/auth)")
  void shouldAllowPublicEndpoints() throws Exception {

    when(request.getRequestURI()).thenReturn("/user/auth");

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar 401 quando Authorization header estiver ausente")
  void shouldReturn401WhenHeaderIsMissing() throws Exception {

    when(request.getRequestURI()).thenReturn("/transaction/test");
    when(request.getHeader("Authorization")).thenReturn(null);

    StringWriter writer = new StringWriter();
    PrintWriter printWriter = new PrintWriter(writer);

    when(response.getWriter()).thenReturn(printWriter);

    filter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar 401 quando token estiver inválido (null)")
  void shouldReturn401WhenTokenIsInvalid() throws Exception {

    when(request.getRequestURI()).thenReturn("/transaction/test");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");

    when(userJWTProvider.validateToken("token")).thenReturn(null);

    filter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  @DisplayName("Deve autenticar usuário com sucesso")
  void shouldAuthenticateUserSuccessfully() throws Exception {

    when(request.getRequestURI()).thenReturn("/transaction/test");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");

    DecodedJWT jwt = mock(DecodedJWT.class);
    Claim claim = mock(Claim.class);

    when(jwt.getSubject()).thenReturn("user-id");
    when(jwt.getClaim("roles")).thenReturn(claim);
    when(claim.asList(Object.class)).thenReturn(List.of("USER"));

    when(userJWTProvider.validateToken("token")).thenReturn(jwt);

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  @DisplayName("Deve retornar 401 quando token estiver expirado")
  void shouldReturn401WhenTokenExpired() throws Exception {

    when(request.getRequestURI()).thenReturn("/transaction/test");
    when(request.getHeader("Authorization")).thenReturn("Bearer token");
    when(response.getWriter()).thenReturn(new PrintWriter(System.out));

    when(userJWTProvider.validateToken(anyString()))
        .thenThrow(new com.auth0.jwt.exceptions.TokenExpiredException("expired", null));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  @DisplayName("Deve retornar 401 quando token Bearer estiver incompleto")
  void shouldReturn401WhenBearerTokenIsIncomplete() throws Exception {

    when(request.getRequestURI()).thenReturn("/transaction/test");
    when(request.getHeader("Authorization")).thenReturn("Bearer");

    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    filter.doFilterInternal(request, response, filterChain);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response).setContentType("application/json");
    verify(response).setCharacterEncoding("UTF-8");
    verify(response).getWriter();
    verify(filterChain, never()).doFilter(request, response);
  }
}