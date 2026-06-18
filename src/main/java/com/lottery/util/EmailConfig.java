package com.lottery.util;

public class EmailConfig {
    
    public static final String SMTP_HOST = getProperty("mail.smtp.host", "smtp.gmail.com");
    public static final String SMTP_PORT = getProperty("mail.smtp.port", "587");
    public static final String SMTP_USERNAME = getProperty("mail.smtp.username", "minhpqfx2007@gmail.com");
    public static final String SMTP_PASSWORD = getProperty("mail.smtp.password", "dody jplo excf tyzy");
    public static final String FROM_EMAIL = getProperty("mail.from.email", SMTP_USERNAME);
    
    public static final int TIMEOUT_MS = 30000; 
    public static final int CONNECTION_TIMEOUT_MS = 15000; 
    
    private static String getProperty(String key, String defaultValue) {
        
        String value = System.getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    public static boolean isValidConfig() {
        return SMTP_USERNAME != null && !SMTP_USERNAME.isEmpty() &&
               SMTP_PASSWORD != null && !SMTP_PASSWORD.isEmpty() &&
               !SMTP_PASSWORD.equals("October13th");
    }
}
