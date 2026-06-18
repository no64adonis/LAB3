package com.lottery.service.validator;

import com.lottery.config.AppConfig;
import com.lottery.service.exception.ServiceException;
import com.lottery.util.InputValidator;

import java.time.LocalDate;

public final class SearchParameterValidator {

    private SearchParameterValidator() {
        
    }

    public static void validateCompany(String company) throws ServiceException {
        if (company == null || company.isEmpty()) {
            return;
        }

        String[] companies = company.split(",");
        for (String comp : companies) {
            String trimmed = comp.trim();
            if (!trimmed.isEmpty() && !InputValidator.isValidCompany(trimmed)) {
                throw new ServiceException("Invalid company format: " + trimmed);
            }
        }
    }

    public static void validateDateRange(LocalDate startDate, LocalDate endDate)
            throws ServiceException {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new ServiceException("Start date cannot be after end date");
        }
    }

    public static void validatePagination(int offset, int limit) throws ServiceException {
        if (offset < 0) {
            throw new ServiceException("Offset must be non-negative");
        }
        if (limit <= 0) {
            throw new ServiceException("Limit must be positive");
        }
        if (limit > AppConfig.MAX_PAGINATION_LIMIT) {
            throw new ServiceException("Limit exceeds maximum: " + AppConfig.MAX_PAGINATION_LIMIT);
        }
    }

    public static void validateUserId(int userId) throws ServiceException {
        if (userId <= 0) {
            throw new ServiceException("Invalid user ID");
        }
    }
}
