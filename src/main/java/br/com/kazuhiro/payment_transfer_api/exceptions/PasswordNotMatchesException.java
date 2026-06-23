package br.com.kazuhiro.payment_transfer_api.exceptions;

public class PasswordNotMatchesException extends RuntimeException {
  public PasswordNotMatchesException() {
    super("As senhas não coincidem!");
  }
}
