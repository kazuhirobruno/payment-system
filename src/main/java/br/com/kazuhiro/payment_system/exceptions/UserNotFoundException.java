package br.com.kazuhiro.payment_system.exceptions;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException() {
    super("Erro na operação solicitada.");
  }
}
