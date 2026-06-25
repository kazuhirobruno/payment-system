package br.com.kazuhiro.payment_system.exceptions;

public class SameAccountTransferException extends RuntimeException {
  public SameAccountTransferException() {
    super("Vetada a transferência para o mesmo usuário.");
  }
}