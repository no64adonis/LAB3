package com.lottery;

import com.lottery.service.DrawService;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("24. LotteryScheduler (Listener)")
class LotterySchedulerTest {

    private LotteryScheduler scheduler;

    @Mock private ServletContextEvent sce;
    @Mock private ServletContext servletContext;
    @Mock private ScheduledExecutorService schedulerService;

    @BeforeEach
    void setUp() {
        scheduler = new LotteryScheduler();
    }

    @Test @DisplayName("#1 contextInitialized - scheduler created")
    void testContextInitialized() throws Exception {
        when(sce.getServletContext()).thenReturn(servletContext);
        scheduler.contextInitialized(sce);
        
        Field f = LotteryScheduler.class.getDeclaredField("scheduler");
        f.setAccessible(true);
        assertNotNull(f.get(scheduler));
    }

    @Test @DisplayName("#2 calculateInitialDelayMillis - positive delay")
    void testCalculateInitialDelay() throws Exception {
        Method m = LotteryScheduler.class.getDeclaredMethod("calculateInitialDelayMillis");
        m.setAccessible(true);
        long delay = (long) m.invoke(scheduler);
        assertTrue(delay >= 0, "Initial delay should be non-negative");
        assertTrue(delay <= 600000, "Initial delay should be at most 10 minutes (600000 ms)");
    }

    @Test @DisplayName("#3 contextDestroyed - scheduler shutdown")
    void testContextDestroyed() throws Exception {
        
        Field f = LotteryScheduler.class.getDeclaredField("scheduler");
        f.setAccessible(true);
        f.set(scheduler, schedulerService);
        scheduler.contextDestroyed(sce);
        verify(schedulerService).shutdownNow();
    }

    @Test @DisplayName("#4 contextDestroyed - scheduler null (no NPE)")
    void testContextDestroyed_nullScheduler() {
        
        assertDoesNotThrow(() -> scheduler.contextDestroyed(sce));
    }

    @Test @DisplayName("#5 contextInitialized - drawService created")
    void testDrawServiceCreated() throws Exception {
        when(sce.getServletContext()).thenReturn(servletContext);
        scheduler.contextInitialized(sce);
        Field f = LotteryScheduler.class.getDeclaredField("drawService");
        f.setAccessible(true);
        assertNotNull(f.get(scheduler));
    }

    @Test @DisplayName("#6 calculateInitialDelayMillis - returns millis")
    void testCalculateInitialDelayMillis_returnsMillis() throws Exception {
        Method m = LotteryScheduler.class.getDeclaredMethod("calculateInitialDelayMillis");
        m.setAccessible(true);
        long delay = (long) m.invoke(scheduler);
        
        assertTrue(delay >= 0 && delay <= 600000L);
    }
}
