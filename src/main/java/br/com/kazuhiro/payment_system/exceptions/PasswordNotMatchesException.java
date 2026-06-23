package br.com.kazuhiro.payment_system.exceptions;

public class PasswordNotMatchesException extends RuntimeException {
  public PasswordNotMatchesException() {
    super("As senhas não coincidem.");
  }
}
