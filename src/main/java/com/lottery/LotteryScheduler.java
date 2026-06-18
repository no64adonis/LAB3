package com.lottery;

import com.lottery.service.DrawService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class LotteryScheduler implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(LotteryScheduler.class.getName());
    private ScheduledExecutorService scheduler;
    private DrawService drawService;
    public void contextInitialized(ServletContextEvent sce) {
        drawService = new DrawService();
        scheduler = Executors.newSingleThreadScheduledExecutor();

        long initialDelay = calculateInitialDelayMillis();
        long period = Duration.ofMinutes(10).toMillis();

        scheduler.scheduleAtFixedRate(() -> {
            try {
                drawService.performDraw();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Scheduled draw failed", e);
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS);

        logger.info("LotteryScheduler initialized. First draw in " + initialDelay + " ms.");
    }
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private long calculateInitialDelayMillis() {
        LocalDateTime now = LocalDateTime.now();
        int minutes = now.getMinute();
        int nextTenMinuteMark = ((minutes / 10) + 1) * 10;

        LocalDateTime nextRun;
        if (nextTenMinuteMark == 60) {
            nextRun = now.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        } else {
            nextRun = now.withMinute(nextTenMinuteMark).withSecond(0).withNano(0);
        }

        return Duration.between(now, nextRun).toMillis();
    }
}