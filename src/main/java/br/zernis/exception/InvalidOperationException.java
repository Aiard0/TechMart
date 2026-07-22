package br.zernis.exception;

public class InvalidOperationException extends BusinessException {

    public InvalidOperationException(String message) {
        super(422, message);
    }
}
