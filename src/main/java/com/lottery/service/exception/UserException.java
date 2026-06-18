package com.lottery.service.exception;

public class UserException extends ServiceException {
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
}