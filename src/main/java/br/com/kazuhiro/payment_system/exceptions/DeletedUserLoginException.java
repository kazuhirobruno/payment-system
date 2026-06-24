package br.com.kazuhiro.payment_system.exceptions;

public class DeletedUserLoginException extends RuntimeException {
  public DeletedUserLoginException() {
    super("Usuário não encontrado.");
  }
}
