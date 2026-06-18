package com.lottery.service;

import com.lottery.db.LotteryTicketSearchCriteria;
import com.lottery.model.LotteryTicket;
import com.lottery.model.PaymentMethod;
import com.lottery.model.User;
import com.lottery.service.exception.ServiceException;
import com.lottery.service.exception.TicketNotFoundException;

import java.util.List;
import java.util.Map;

public interface AdminService {

        List<LotteryTicket> searchAllTickets(LotteryTicketSearchCriteria criteria) throws ServiceException;

        int getTicketCount(LotteryTicketSearchCriteria criteria) throws ServiceException;

        boolean updateTicketStatus(String ticketId, boolean published) throws TicketNotFoundException, ServiceException;

        boolean updateTicketStatus(List<String> ticketIds, boolean published) throws ServiceException;

        boolean updatePriceForCompany(String company, double price) throws ServiceException;

        boolean bulkDeactivateUsers(List<Integer> userIds) throws ServiceException;

        List<String> getAllCompanies() throws ServiceException;

        double getPriceForCompany(String company) throws ServiceException;

        List<LotteryTicket> getAllTickets(int offset, int limit) throws ServiceException;

        int getTotalTicketCount() throws ServiceException;

        LotteryTicket getTicketById(String ticketId) throws TicketNotFoundException, ServiceException;

        List<User> getInactiveUsers(String period) throws ServiceException;

        boolean createLotteryTicket(LotteryTicket ticket) throws ServiceException;

        boolean updateUserRole(int userId, String role) throws ServiceException;

        boolean setUserActiveStatus(int userId, boolean isActive) throws ServiceException;

        boolean updateUserDetails(int userId, String email, String firstName, String lastName, String phone)
                        throws ServiceException;

        boolean sendInvitation(String email) throws ServiceException;

        boolean processPriceUpdates(Map<String, String> priceParams)
                        throws ServiceException;

        LotteryTicket processTicketCreation(
                        String ticketID, String num1, String num2, String num3, String num4, String num5, String num6,
                        String company, String publishedStr)
                        throws ServiceException;

        boolean processTicketStatusUpdate(String ticketId, String publishedStr)
                        throws ServiceException, TicketNotFoundException;

        boolean processBulkTicketStatusUpdate(String selectedTicketsStr, String publishedStr)
                        throws ServiceException;

        boolean processBulkUserDeactivation(String[] userIdsStr)
                        throws ServiceException;

        boolean processUserRoleUpdate(String userIdStr, String role)
                        throws ServiceException;

        boolean processUserActiveStatusUpdate(String userIdStr, String isActiveStr)
                        throws ServiceException;

        boolean processUserDetailsUpdate(String userIdStr, String email, String firstName, String lastName,
                        String phone)
                        throws ServiceException;

        boolean processUserCreation(String email, String password, String firstName, String lastName, String role,
                        String balanceStr, boolean isActive)
                        throws ServiceException;

        List<PaymentMethod> getAllPaymentMethodsWithUsers(int offset, int limit)
                        throws ServiceException;

        int getTotalPaymentMethodCount()
                        throws ServiceException;

        boolean updatePaymentMethod(PaymentMethod paymentMethod)
                        throws ServiceException;

        boolean deletePaymentMethod(int paymentMethodId)
                        throws ServiceException;

        boolean bulkDeletePaymentMethods(String[] paymentMethodIds)
                        throws ServiceException;

        java.util.Map<String, Object> processBulkTicketInsertion(String csvData)
                        throws ServiceException;

        boolean createPaymentMethod(PaymentMethod paymentMethod)
                        throws ServiceException;
}