package com.lottery.service.exception;

public class TicketException extends ServiceException {
    public TicketException(String message) {
        super(message);
    }
    
    public TicketException(String message, Throwable cause) {
        super(message, cause);
    }
}