package br.com.kazuhiro.payment_system.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.TokenExpiredException;

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

    String header = request.getHeader("Authorization");
    String requestURI = request.getRequestURI();
    String normalizedURI = requestURI.replaceAll("/+$", "");
    String contentApplicationJson = "application/json";
    String contentCharEncoding = "UTF-8";

    boolean isUserRegisterUrl = normalizedURI.equalsIgnoreCase("/user");
    boolean isUserAuthUrl = normalizedURI.equalsIgnoreCase("/user/auth");

    boolean isUserEndpoint = requestURI.startsWith("/user");
    boolean isTransactionEndpoint = requestURI.startsWith("/transaction");
    if (isTransactionEndpoint || isUserEndpoint) {
      if (isUserRegisterUrl || isUserAuthUrl) {
        filterChain.doFilter(request, response);
        return;
      }
      if (header == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(contentApplicationJson);
        response.setCharacterEncoding(contentCharEncoding);
        response.getWriter()
            .write("{\"message\": \"O token de autenticação é obrigatório para acessar este recurso.\"}");
        return;
      }
      String tokenPuro = header.startsWith("Bearer ") ? header.substring(7) : header;
      if (tokenPuro.equals("Bearer")) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(contentApplicationJson);
        response.setCharacterEncoding(contentCharEncoding);
        response.getWriter().write("{\"message\": \"O token JWT enviado está incompleto ou inválido.\"}");
        return;
      }

      try {
        var token = this.userJWTProvider.validateToken(tokenPuro);

        if (token == null) {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          return;
        }

        request.setAttribute("user_id", token.getSubject());

        var roles = token.getClaim("roles").asList(Object.class);
        var grants = roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
            .toList();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(token.getSubject(), null,
            grants);
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (TokenExpiredException e) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(contentApplicationJson);
        response.setCharacterEncoding(contentCharEncoding);
        response.getWriter().write("{\"message\": \"O token enviado está expirado. Faça login novamente.\"}");
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

}
