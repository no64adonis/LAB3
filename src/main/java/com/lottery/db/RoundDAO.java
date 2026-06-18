package com.lottery.db;

import com.lottery.model.Round;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RoundDAO extends BaseDAO {

    public static Round createRound(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            logger.log(Level.WARNING, "Invalid round dates provided: start=" + startDate + ", end=" + endDate);
            return null;
        }

        String query = "INSERT INTO Rounds (StartDate, EndDate, Status) VALUES (?, ?, 'ACTIVE')";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDate(1, Date.valueOf(startDate));
            statement.setDate(2, Date.valueOf(endDate));

            int rows = statement.executeUpdate();
            if (rows == 0) {
                logger.warning("Failed to create round, no rows affected.");
                return null;
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    Round round = new Round();
                    round.setRoundID(keys.getInt(1));
                    round.setStartDate(startDate);
                    round.setEndDate(endDate);
                    round.setStatus("ACTIVE");
                    return round;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating round", e);
        }

        return null;
    }

    public static Round getCurrentRound() {
        String query = "SELECT TOP 1 * FROM Rounds WHERE Status = 'ACTIVE' ORDER BY RoundID DESC";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return mapRound(resultSet);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving current round", e);
        }

        return null;
    }

    public static boolean completeRound(int roundId, String winningNumbers) {
        if (roundId <= 0 || winningNumbers == null || winningNumbers.isEmpty()) {
            logger.log(Level.WARNING, "Invalid round completion parameters. roundId=" + roundId);
            return false;
        }

        String query = "UPDATE Rounds SET WinningNumbers = ?, Status = 'COMPLETED' WHERE RoundID = ?";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, winningNumbers);
            statement.setInt(2, roundId);

            int rows = statement.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error completing round: " + roundId, e);
            return false;
        }
    }

    public static List<Round> getPastRoundsForUser(int userId) {
        List<Round> rounds = new ArrayList<>();
        if (userId <= 0) {
            return rounds;
        }

        String query = "SELECT * FROM Rounds WHERE Status = 'COMPLETED' "
                + "AND RoundID IN (SELECT DISTINCT RoundID FROM UserTicketHistory WHERE UserID = ?) "
                + "ORDER BY EndDate DESC";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rounds.add(mapRound(resultSet));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving past rounds for user: " + userId, e);
        }

        return rounds;
    }

    public static List<Round> getAllPastRounds() {
        List<Round> rounds = new ArrayList<>();
        String query = "SELECT * FROM Rounds WHERE Status = 'COMPLETED' ORDER BY EndDate DESC";

        try (Connection connection = DatabaseConfig.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rounds.add(mapRound(resultSet));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error retrieving past rounds", e);
        }

        return rounds;
    }

    private static Round mapRound(ResultSet resultSet) throws SQLException {
        Round round = new Round();
        round.setRoundID(resultSet.getInt("RoundID"));
        Date startDate = resultSet.getDate("StartDate");
        if (startDate != null) {
            round.setStartDate(startDate.toLocalDate());
        }
        Date endDate = resultSet.getDate("EndDate");
        if (endDate != null) {
            round.setEndDate(endDate.toLocalDate());
        }
        round.setWinningNumbers(resultSet.getString("WinningNumbers"));
        round.setStatus(resultSet.getString("Status"));
        if (resultSet.getTimestamp("CreatedAt") != null) {
            round.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
        }
        return round;
    }
}
