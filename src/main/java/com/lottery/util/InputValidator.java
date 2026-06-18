package com.lottery.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.regex.Pattern;
import org.owasp.encoder.Encode;

public class InputValidator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern COMPANY_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_&.'\\/]{1,100}$");
    private static final Pattern TICKET_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-_]{1,50}$");
    private static final Pattern NUMBERS_PATTERN = Pattern.compile("^([1-9][0-9]?)(,([1-9][0-9]?)){5}$");

    public static boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(identifier).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isAuthenticEmail(String email) {
        if (email == null || email.isEmpty() || !isValidEmail(email)) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf('@') + 1);

        return domain.contains(".") && !domain.endsWith(".") && domain.length() > 3;
    }

    public static boolean isValidCompany(String company) {
        if (company == null || company.isEmpty()) {
            return false;
        }
        return COMPANY_PATTERN.matcher(company).matches();
    }

    public static boolean isValidTicketId(String ticketId) {
        if (ticketId == null || ticketId.isEmpty()) {
            return false;
        }
        return TICKET_ID_PATTERN.matcher(ticketId).matches();
    }

    public static boolean isValidNumbers(String numbers) {
        if (numbers == null || numbers.isEmpty()) {
            return false;
        }
        return NUMBERS_PATTERN.matcher(numbers).matches();
    }

    public static boolean isValidLotteryNumber(int number) {
        return number >= 1 && number <= 99;
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static Optional<LocalDate> parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(dateStr));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static boolean isValidDateRange(String startDateStr, String endDateStr) {
        if (!isValidDate(startDateStr) || !isValidDate(endDateStr)) {
            return false;
        }

        try {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            return !startDate.isAfter(endDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static String sanitizeString(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"'&]", "")
                .trim();
    }

    
    public static String sanitizeForHtml(String input) {
        if (input == null)
            return null;
        return Encode.forHtml(input);
    }

    
    public static String sanitizeForJavaScript(String input) {
        if (input == null)
            return null;
        return Encode.forJavaScript(input);
    }

    
    public static String sanitizeUrlParameter(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[<>\"'&?=#]", "")
                .trim();
    }

    
    public static String sanitizeForSqlLike(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replaceAll("[<>\"'&]", "")
                .trim();
    }

    public static int validatePageNumber(String pageStr, int maxPage) {
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) {
                    page = 1;
                } else if (maxPage > 0 && page > maxPage) {
                    page = maxPage;
                }
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        return page;
    }

    public static boolean isValidSearchTerm(String searchTerm, int maxLength) {
        if (searchTerm == null) {
            return true; 
        }
        return searchTerm.length() <= maxLength;
    }

    public static boolean containsSQLInjectionPatterns(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        String lowerInput = input.toLowerCase();
        String[] sqlKeywords = {
                "select", "insert", "update", "delete", "drop", "create", "alter", "exec",
                "union", "script", "javascript", "vbscript", "expression", "applet"
        };

        for (String keyword : sqlKeywords) {
            if (lowerInput.contains(keyword)) {
                
                if (lowerInput.matches(".*[^a-zA-Z]" + keyword + "[^a-zA-Z].*") ||
                        lowerInput.matches("^" + keyword + "[^a-zA-Z].*") ||
                        lowerInput.matches(".*[^a-zA-Z]" + keyword + "$")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Integer validateInteger(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String sanitized = input.trim().replaceAll("[^0-9-]", "");

        if (sanitized.indexOf('-') > 0) {
            sanitized = sanitized.replace("-", "");
        }

        try {
            return Integer.parseInt(sanitized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double validateDouble(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        String sanitized = input.trim().replaceAll("[^0-9.-]", "");

        int decimalPointCount = 0;
        StringBuilder cleaned = new StringBuilder();
        boolean firstChar = true;

        for (char c : sanitized.toCharArray()) {
            if (c == '-' && firstChar) {
                cleaned.append(c);
                firstChar = false;
            } else if (c == '.' && decimalPointCount == 0) {
                cleaned.append(c);
                decimalPointCount++;
                firstChar = false;
            } else if (Character.isDigit(c)) {
                cleaned.append(c);
                firstChar = false;
            }
        }

        try {
            return Double.parseDouble(cleaned.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Integer validateAndSanitizeInteger(String input, int minValue, int maxValue) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        Integer value = validateInteger(input);

        if (value != null && value >= minValue && value <= maxValue) {
            return value;
        }

        return null;
    }

    public static Double validateAndSanitizeDouble(String input, double minValue, double maxValue) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        Double value = validateDouble(input);

        if (value != null && value >= minValue && value <= maxValue) {
            return value;
        }

        return null;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return true; 
        }
        
        return phone.matches("^[+]?[0-9\\s\\-()]{7,20}$");
    }

    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        return name.matches("^[a-zA-Z\\s\\-']{1,50}$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        
        return password.length() >= 8 && password.length() <= 100;
    }

    public static boolean isValidRole(String role) {
        if (role == null || role.isEmpty()) {
            return false;
        }
        
        return "user".equals(role) || "admin".equals(role) || "none".equals(role);
    }

    public static boolean parseBooleanParameter(String param) {
        return "true".equals(param) || "on".equals(param);
    }

    public static boolean isValidPagination(int offset, int limit) {
        return offset >= 0 && limit > 0 && limit <= 1000;
    }
}