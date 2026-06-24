package br.com.kazuhiro.payment_system.modules.user.usecases;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import javax.security.sasl.AuthenticationException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import br.com.kazuhiro.payment_system.exceptions.DeletedUserLoginException;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.dtos.AuthUserResponseDTO;
import br.com.kazuhiro.payment_system.modules.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthUserUseCase {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${security.token.secret.user}")
  private String secretkey;

  public AuthUserResponseDTO execute(AuthUserRequestDTO authUserRequestDTO) throws AuthenticationException {
    String exceptionMessage = "Email/senha incorreto.";
    var user = this.userRepository.findByEmail(authUserRequestDTO.getEmail()).orElseThrow(() -> {
      throw new UsernameNotFoundException(exceptionMessage);
    });
    var passwordMatches = this.passwordEncoder.matches(authUserRequestDTO.getPassword(), user.getPassword());

    if (!passwordMatches) {
      throw new AuthenticationException(exceptionMessage);
    }

    if (!user.isActive()) {
      throw new DeletedUserLoginException();
    }

    Algorithm algorithm = Algorithm.HMAC256(secretkey);

    var expiresIn = Instant.now().plus(Duration.ofMinutes(60));
    var roles = Arrays.asList("USER");
    var token = JWT.create()
        .withIssuer("payment-system")
        .withSubject(user.getId().toString())
        .withClaim("roles", roles)
        .withExpiresAt(expiresIn)
        .sign(algorithm);

    return AuthUserResponseDTO.builder().expiresAt(expiresIn).roles(roles).token(token).build();
  }
}
