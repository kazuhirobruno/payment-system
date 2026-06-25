package br.com.kazuhiro.payment_system.exceptions;

public class ReceiverUserInactiveException extends RuntimeException {
  public ReceiverUserInactiveException() {
    super("Usuário não encontrado.");
  }
}
