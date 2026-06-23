package br.com.kazuhiro.payment_system.modules.user.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.kazuhiro.payment_system.exceptions.PasswordNotMatchesException;
import br.com.kazuhiro.payment_system.exceptions.UserFoundException;
import br.com.kazuhiro.payment_system.modules.user.dtos.CreateUserRequestDTO;
import br.com.kazuhiro.payment_system.modules.user.usecases.CreateUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
  private final CreateUserUseCase createUserUseCase;

  @PostMapping("/")
  public ResponseEntity<Object> create(@Valid @RequestBody CreateUserRequestDTO createUserRequestDTO) {
    try {
      var response = this.createUserUseCase.execute(createUserRequestDTO);
      return ResponseEntity.ok(response);
    } catch (UserFoundException | PasswordNotMatchesException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Erro ao processar o cadastro.");
    }
  }
}
