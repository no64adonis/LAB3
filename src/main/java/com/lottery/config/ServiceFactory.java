package com.lottery.config;

import com.lottery.service.*;

public class ServiceFactory {
    private static ServiceFactory instance;
    private TicketService ticketService;
    private UserService userService;
    private AuthService authService;
    private PaymentService paymentService;
    private AdminService adminService;
    private EmailService emailService;
    private SearchHistoryService searchHistoryService;
    private TransactionService transactionService;
    private CardValidationService cardValidationService;

    private ServiceFactory() {
        
        this.ticketService = new TicketServiceImpl();
        this.userService = new UserServiceImpl();
        this.authService = new AuthServiceImpl();
        this.paymentService = new PaymentServiceImpl();
        this.adminService = new AdminServiceImpl();
        this.emailService = new EmailServiceImpl();
        this.searchHistoryService = new SearchHistoryServiceImpl();
        this.transactionService = new TransactionServiceImpl();
        this.cardValidationService = new CardValidationServiceImpl();
    }

    public static synchronized ServiceFactory getInstance() {
        if (instance == null) {
            instance = new ServiceFactory();
        }
        return instance;
    }

    public TicketService getTicketService() {
        return ticketService;
    }

    public UserService getUserService() {
        return userService;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public SearchHistoryService getSearchHistoryService() {
        return searchHistoryService;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public CardValidationService getCardValidationService() {
        return cardValidationService;
    }

    public void setTicketService(TicketService service) {
        this.ticketService = service;
    }

    public void setUserService(UserService service) {
        this.userService = service;
    }

    public void setCardValidationService(CardValidationService service) {
        this.cardValidationService = service;
    }
}
