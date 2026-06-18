package com.lottery.db;

import com.lottery.model.LotteryTicket;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LotteryTicketResultSetMapper {

    public static LotteryTicket mapResultSetToLotteryTicket(ResultSet resultSet) throws SQLException {
        LotteryTicket ticket = new LotteryTicket();
        ticket.setTicketID(resultSet.getString("TicketID"));
        
        StringBuilder numbersBuilder = new StringBuilder();
        numbersBuilder.append(resultSet.getInt("Number 1"));
        numbersBuilder.append(",");
        numbersBuilder.append(resultSet.getInt("Number 2"));
        numbersBuilder.append(",");
        numbersBuilder.append(resultSet.getInt("Number 3"));
        numbersBuilder.append(",");
        numbersBuilder.append(resultSet.getInt("Number 4"));
        numbersBuilder.append(",");
        numbersBuilder.append(resultSet.getInt("Number 5"));
        numbersBuilder.append(",");
        numbersBuilder.append(resultSet.getInt("Number 6"));
        ticket.setNumbers(numbersBuilder.toString());
        ticket.setCompany(resultSet.getString("Company"));
        ticket.setCreationDate(resultSet.getDate("CreationDate").toLocalDate());
        ticket.setPublished(resultSet.getBoolean("Published"));
        ticket.setViewCount(resultSet.getInt("ViewCount"));
        
        ticket.setPrice(resultSet.getDouble("Price"));
        
        int ownerID = resultSet.getInt("OwnerID");
        if (!resultSet.wasNull()) {
            ticket.setOwnerId(String.valueOf(ownerID));
        } else {
            ticket.setOwnerId(null);
        }
        return ticket;
    }
}