package com.lottery.service.exception;

public class TicketNotFoundException extends TicketException {
    private final String ticketId;
    
    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
        this.ticketId = ticketId;
    }
    
    public TicketNotFoundException(String ticketId, Throwable cause) {
        super("Ticket not found: " + ticketId, cause);
        this.ticketId = ticketId;
    }
    
    public String getTicketId() {
        return ticketId;
    }
}