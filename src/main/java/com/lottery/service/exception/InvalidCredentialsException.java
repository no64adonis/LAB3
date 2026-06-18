package com.lottery.service.exception;

public class InvalidCredentialsException extends UserException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}