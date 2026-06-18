package com.lottery.service;

import com.lottery.db.UserSearchHistoryDAO;
import com.lottery.db.UserSearchHistoryDAO.SearchHistoryEntry;
import com.lottery.service.exception.ServiceException;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchHistoryServiceImpl implements SearchHistoryService {

    private static final Logger logger = Logger.getLogger(SearchHistoryServiceImpl.class.getName());
    public boolean recordSearch(int userId, String searchPrompt) throws ServiceException {
        try {
            if (UserSearchHistoryDAO.searchPromptExists(userId, searchPrompt)) {
                return false;
            }
            return UserSearchHistoryDAO.createSearchHistory(userId, searchPrompt);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error recording search history", e);
            throw new ServiceException("Error recording search history: " + e.getMessage(), e);
        }
    }
    public void updateSearchTimestamp(int searchId) throws ServiceException {
        try {
            UserSearchHistoryDAO.updateSearchHistoryDate(searchId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating search timestamp", e);
            throw new ServiceException("Error updating search timestamp: " + e.getMessage(), e);
        }
    }
    public List<SearchHistoryEntry> getSearchHistory(int userId, int limit) throws ServiceException {
        try {
            return UserSearchHistoryDAO.getUserSearchHistory(userId, limit);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving search history", e);
            throw new ServiceException("Error retrieving search history: " + e.getMessage(), e);
        }
    }
    public void clearSearchHistory(int userId) throws ServiceException {
        try {
            UserSearchHistoryDAO.clearUserSearchHistory(userId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error clearing search history", e);
            throw new ServiceException("Error clearing search history: " + e.getMessage(), e);
        }
    }
    public boolean searchExists(int userId, String searchPrompt) throws ServiceException {
        try {
            return UserSearchHistoryDAO.searchPromptExists(userId, searchPrompt);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error checking if search exists", e);
            throw new ServiceException("Error checking if search exists: " + e.getMessage(), e);
        }
    }
}
