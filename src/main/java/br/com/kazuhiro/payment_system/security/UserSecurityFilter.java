package br.com.kazuhiro.payment_system.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;

import br.com.kazuhiro.payment_system.providers.UserJWTProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserSecurityFilter extends OncePerRequestFilter {
  private final UserJWTProvider userJWTProvider;

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String requestURI = request.getRequestURI();

    if (!isMonitoredEndpoint(requestURI) || isPublicRoute(requestURI)) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");
    if (header == null) {
      sendUnauthorizedResponse(response, "O token de autenticação é obrigatório para acessar este recurso.");
      return;
    }

    String tokenPuro = header.startsWith("Bearer ") ? header.substring(7) : header;
    if (tokenPuro.equals("Bearer")) {
      sendUnauthorizedResponse(response, "O token JWT enviado está incompleto ou inválido.");
      return;
    }
    try {
      var token = this.userJWTProvider.validateToken(tokenPuro);
      if (token == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return;
      }
      authenticateUser(request, token);
    } catch (TokenExpiredException e) {
      sendUnauthorizedResponse(response, "O token enviado está expirado. Faça login novamente.");
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isMonitoredEndpoint(String requestURI) {
    if (requestURI == null || requestURI.isEmpty()) {
      return false;
    }
    return requestURI.startsWith("/user") || requestURI.startsWith("/transaction");
  }

  boolean isPublicRoute(String requestURI) {
    if (requestURI == null || requestURI.isEmpty()) {
      return false;
    }

    int end = requestURI.length();
    while (end > 0 && requestURI.charAt(end - 1) == '/') {
      end--;
    }

    String normalizedURI = (end == 0) ? "/" : requestURI.substring(0, end);
    return normalizedURI.equalsIgnoreCase("/user") || normalizedURI.equalsIgnoreCase("/user/auth");
  }

  private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write("{\"message\": \"" + message + "\"}");
  }

  private void authenticateUser(HttpServletRequest request, DecodedJWT token) {
    request.setAttribute("user_id", token.getSubject());

    var roles = token.getClaim("roles").asList(Object.class);
    var grants = roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
        .toList();

    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
        token.getSubject(), null, grants);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}