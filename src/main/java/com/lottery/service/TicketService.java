package com.lottery.service;

import com.lottery.model.LotteryTicket;
import com.lottery.model.User;
import com.lottery.service.exception.InsufficientBalanceException;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;
import com.lottery.service.exception.UserNotFoundException;
import java.time.LocalDate;
import java.util.List;

public interface TicketService {

        List<LotteryTicket> searchAvailableTickets(String company, LocalDate specificDate, LocalDate startDate,
                        LocalDate endDate, int offset, int limit) throws ServiceException;

        LotteryTicket selectTicket(List<LotteryTicket> availableTickets, String numbers)
                        throws TicketNotFoundException, ServiceException;

        boolean purchaseTicket(LotteryTicket ticket, User user) throws InsufficientBalanceException, ServiceException;

        List<LotteryTicket> getUserTickets(int userId, int offset, int limit)
                        throws UserNotFoundException, ServiceException;

        int getUserTicketCount(int userId) throws UserNotFoundException, ServiceException;

        List<LotteryTicket> searchPublishedTickets(String company, LocalDate specificDate, LocalDate startDate,
                        LocalDate endDate, int offset, int limit) throws ServiceException;

        List<String> getAllPublishedCompanies() throws ServiceException;

        List<String> getAllDistinctCompanies() throws ServiceException;

        int getPublishedTicketCount(String company, LocalDate specificDate, LocalDate startDate, LocalDate endDate)
                        throws ServiceException;

        boolean incrementViewCount(String ticketId) throws ServiceException;

        boolean incrementViewCountBatch(List<String> ticketIds) throws ServiceException;

        LotteryTicket getTicketById(String ticketId) throws TicketNotFoundException, ServiceException;

        List<String> purchaseMultipleTickets(List<String> ticketIds, User user)
                        throws InsufficientBalanceException, ServiceException;

        List<LotteryTicket> searchPublishedTicketsByNumbersAndDate(String numbers, LocalDate startDate,
                        LocalDate endDate,
                        int offset, int limit) throws ServiceException;

        List<LotteryTicket> searchPublishedTicketsByNumbersAndDate(String numbers, LocalDate specificDate, int offset,
                        int limit) throws ServiceException;

        List<LotteryTicket> searchPublishedTicketsByNumbersAndCompany(String numbers, String company, int offset,
                        int limit)
                        throws ServiceException;

        List<LotteryTicket> searchPublishedTicketsByNumbers(String numbers, int offset, int limit)
                        throws ServiceException;

        SearchResult processUserLotterySearch(
                        String[] companyParams, String num1, String num2, String num3, String num4, String num5,
                        String num6,
                        String startDateStr, String endDateStr, String specificDateStr, String pageParam)
                        throws ServiceException;

        LotteryTicket processTicketPurchase(
                        String company, String num1, String num2, String num3, String num4, String num5, String num6,
                        String startDateStr, String endDateStr, String specificDateStr, User user)
                        throws ServiceException, InsufficientBalanceException, TicketNotFoundException;

        SearchResult searchTicketsForPurchase(
                        String company, String num1, String num2, String num3, String num4, String num5, String num6,
                        String startDateStr, String endDateStr, String specificDateStr, String pageParam)
                        throws ServiceException;

        int getPublishedTicketCountByNumbersAndDate(String numbers, LocalDate startDate, LocalDate endDate)
                        throws ServiceException;

        int getPublishedTicketCountByNumbersAndDate(String numbers, LocalDate specificDate) throws ServiceException;

        int getPublishedTicketCountByNumbersAndCompany(String numbers, String company) throws ServiceException;

        int getPublishedTicketCountByNumbers(String numbers) throws ServiceException;

        SearchResult processUserTickets(int userId, String pageParam)
                        throws ServiceException, UserNotFoundException;
}
