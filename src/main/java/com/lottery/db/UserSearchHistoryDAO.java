package com.lottery.db;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class UserSearchHistoryDAO extends BaseDAO {

    public static boolean createSearchHistory(int userId, String searchPrompt) {
        String query = "INSERT INTO UserSearchHistory (UserID, SearchPrompt, SearchDate) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, searchPrompt);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating search history for user ID: " + userId, e);
        }

        return false;
    }

    public static List<SearchHistoryEntry> getUserSearchHistory(int userId, int limit) {
        List<SearchHistoryEntry> history = new ArrayList<>();
        String query = "SELECT SearchID, SearchPrompt, SearchDate FROM UserSearchHistory WHERE UserID = ? ORDER BY SearchDate DESC OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setInt(2, limit);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                SearchHistoryEntry entry = new SearchHistoryEntry();
                entry.setSearchId(resultSet.getInt("SearchID"));
                entry.setSearchPrompt(resultSet.getString("SearchPrompt"));
                entry.setSearchDate(resultSet.getTimestamp("SearchDate").toLocalDateTime());
                history.add(entry);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting search history for user ID: " + userId, e);
        }

        return history;
    }

    public static boolean updateSearchHistoryDate(int searchId) {
        String query = "UPDATE UserSearchHistory SET SearchDate = ? WHERE SearchID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setInt(2, searchId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating search history date for search ID: " + searchId, e);
        }

        return false;
    }

    public static boolean searchPromptExists(int userId, String searchPrompt) {
        String query = "SELECT COUNT(*) AS count FROM UserSearchHistory WHERE UserID = ? AND SearchPrompt = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);
            statement.setString(2, searchPrompt);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("count") > 0;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error checking if search prompt exists for user ID: " + userId, e);
        }

        return false;
    }

    public static boolean clearUserSearchHistory(int userId) {
        String query = "DELETE FROM UserSearchHistory WHERE UserID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error clearing search history for user ID: " + userId, e);
        }

        return false;
    }

    public static class SearchHistoryEntry {
        private int searchId;
        private String searchPrompt;
        private LocalDateTime searchDate;

        public int getSearchId() {
            return searchId;
        }

        public void setSearchId(int searchId) {
            this.searchId = searchId;
        }

        public String getSearchPrompt() {
            return searchPrompt;
        }

        public void setSearchPrompt(String searchPrompt) {
            this.searchPrompt = searchPrompt;
        }

        public LocalDateTime getSearchDate() {
            return searchDate;
        }

        public void setSearchDate(LocalDateTime searchDate) {
            this.searchDate = searchDate;
        }
    }
}
