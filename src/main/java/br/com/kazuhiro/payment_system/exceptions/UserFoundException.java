package br.com.kazuhiro.payment_system.exceptions;

public class UserFoundException extends RuntimeException {
  public UserFoundException() {
    super("Erro na operação solicitada!");
  }
}
