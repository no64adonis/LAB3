package com.lottery.service.exception;

public class ValidationException extends ServiceException {
    private final String field;
    private final String value;
    
    public ValidationException(String field, String value) {
        super("Validation failed for field '" + field + "' with value '" + value + "'");
        this.field = field;
        this.value = value;
    }
    
    public ValidationException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }
    
    public String getField() {
        return field;
    }
    
    public String getValue() {
        return value;
    }
}