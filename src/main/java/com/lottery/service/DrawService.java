package com.lottery.service;

import com.lottery.config.ServiceFactory;
import com.lottery.db.LotteryTicketDAO;
import com.lottery.db.RoundDAO;
import com.lottery.db.UserTicketHistoryDAO;
import com.lottery.model.LotteryTicket;
import com.lottery.model.Round;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DrawService {
    private static final Logger logger = Logger.getLogger(DrawService.class.getName());
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 99;

    private static final Map<Integer, BigDecimal> PAYOUTS;
    static {
        Map<Integer, BigDecimal> payouts = new HashMap<>();
        payouts.put(3, new BigDecimal("50"));
        payouts.put(4, new BigDecimal("200"));
        payouts.put(5, new BigDecimal("1000"));
        payouts.put(6, new BigDecimal("10000"));
        PAYOUTS = Collections.unmodifiableMap(payouts);
    }

    private final UserService userService;
    private final Random random = new Random();

    public DrawService() {
        this.userService = ServiceFactory.getInstance().getUserService();
    }

    public synchronized void performDraw() {
        try {
            Round currentRound = RoundDAO.getCurrentRound();
            if (currentRound == null) {
                LocalDate today = LocalDate.now();
                currentRound = RoundDAO.createRound(today, today);
            }

            if (currentRound == null) {
                logger.warning("No active round found or created. Draw aborted.");
                return;
            }

            String winningNumbers = generateWinningNumbers();
            boolean roundCompleted = RoundDAO.completeRound(currentRound.getRoundID(), winningNumbers);
            if (!roundCompleted) {
                logger.warning("Failed to mark round as completed. Draw aborted.");
                return;
            }

            List<LotteryTicket> tickets = LotteryTicketDAO.getPurchasedTickets();
            for (LotteryTicket ticket : tickets) {
                try {
                    int matchCount = countMatches(ticket.getNumbers(), winningNumbers);
                    BigDecimal winnings = PAYOUTS.getOrDefault(matchCount, BigDecimal.ZERO);
                    int ownerId = Integer.parseInt(ticket.getOwnerId());

                    if (winnings.compareTo(BigDecimal.ZERO) > 0) {
                        userService.updateUserBalance(ownerId, winnings);
                    }

                    UserTicketHistoryDAO.archiveTicket(
                            ownerId,
                            ticket.getTicketID(),
                            ticket.getNumbers(),
                            ticket.getCompany(),
                            currentRound.getRoundID(),
                            BigDecimal.valueOf(ticket.getPrice()),
                            winnings,
                            matchCount);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error processing ticket: " + ticket.getTicketID(), e);
                }
            }

            LotteryTicketDAO.resetTicketsForNewRound();

            LocalDate nextStart = LocalDate.now();
            RoundDAO.createRound(nextStart, nextStart);
            logger.info("Draw completed for round: " + currentRound.getRoundID());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error performing draw", e);
        }
    }

    private String generateWinningNumbers() {
        List<Integer> numbers = new ArrayList<>();
        while (numbers.size() < 6) {
            int candidate = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
            if (!numbers.contains(candidate)) {
                numbers.add(candidate);
            }
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < numbers.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }
            builder.append(numbers.get(i));
        }
        return builder.toString();
    }

    private int countMatches(String ticketNumbers, String winningNumbers) {
        if (ticketNumbers == null || winningNumbers == null) {
            return 0;
        }
        String[] ticketParts = ticketNumbers.split(",");
        String[] winningParts = winningNumbers.split(",");
        int matchCount = 0;
        for (int i = 0; i < Math.min(ticketParts.length, winningParts.length); i++) {
            if (ticketParts[i].trim().equals(winningParts[i].trim())) {
                matchCount++;
            }
        }
        return matchCount;
    }
}
