package com.lottery.service;

import com.lottery.db.UserSearchHistoryDAO;
import com.lottery.db.UserSearchHistoryDAO.SearchHistoryEntry;
import com.lottery.service.exception.ServiceException;

import java.util.List;

public interface SearchHistoryService {

    boolean recordSearch(int userId, String searchPrompt) throws ServiceException;

    void updateSearchTimestamp(int searchId) throws ServiceException;

    List<SearchHistoryEntry> getSearchHistory(int userId, int limit) throws ServiceException;

    void clearSearchHistory(int userId) throws ServiceException;

    boolean searchExists(int userId, String searchPrompt) throws ServiceException;
}
