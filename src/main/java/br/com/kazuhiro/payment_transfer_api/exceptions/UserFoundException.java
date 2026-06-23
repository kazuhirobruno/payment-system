package br.com.kazuhiro.payment_transfer_api.exceptions;

public class UserFoundException extends RuntimeException {
  public UserFoundException() {
    super("Erro na operação solicitada!");
  }
}
