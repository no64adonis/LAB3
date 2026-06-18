package com.lottery.service.exception;

public class UserNotFoundException extends UserException {
    private final String identifier;
    
    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
        this.identifier = identifier;
    }
    
    public UserNotFoundException(String identifier, Throwable cause) {
        super("User not found: " + identifier, cause);
        this.identifier = identifier;
    }
    
    public String getIdentifier() {
        return identifier;
    }
}