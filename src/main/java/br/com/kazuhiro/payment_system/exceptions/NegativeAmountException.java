package br.com.kazuhiro.payment_system.exceptions;

public class NegativeAmountException extends RuntimeException {
  public NegativeAmountException() {
    super("Saldo insuficiente.");
  }
}
