package com.lottery.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.lottery.config.AppConfig;

public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());
    private static final String SESSION_TOKEN_ATTR = "sessionToken";
    private static final String SESSION_IP_ATTR = "clientIP";
    private static final String SESSION_USER_AGENT_ATTR = "userAgent";
    private static final String SESSION_CREATION_TIME_ATTR = "sessionCreationTime";
    private static final String SESSION_LAST_ACTIVITY_ATTR = "lastActivityTime";

    public static HttpSession createSecureSession(HttpServletRequest request, HttpServletResponse response,
            String email) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);

        String sessionToken = generateSecureToken();
        session.setAttribute(SESSION_TOKEN_ATTR, sessionToken);
        session.setAttribute(SESSION_IP_ATTR, getClientIP(request));
        session.setAttribute(SESSION_USER_AGENT_ATTR, request.getHeader("User-Agent"));

        long currentTime = System.currentTimeMillis();
        session.setAttribute(SESSION_CREATION_TIME_ATTR, currentTime);
        session.setAttribute(SESSION_LAST_ACTIVITY_ATTR, currentTime);
        session.setAttribute("email", email);

        return session;
    }

    public static boolean validateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Long lastActivityTime = (Long) session.getAttribute(SESSION_LAST_ACTIVITY_ATTR);
        if (lastActivityTime == null
                || (System.currentTimeMillis() - lastActivityTime) > AppConfig.SESSION_TIMEOUT_MS) {
            invalidateSession(request, response);
            return false;
        }

        String storedIP = (String) session.getAttribute(SESSION_IP_ATTR);
        String currentIP = getClientIP(request);
        if (storedIP == null || !storedIP.equals(currentIP)) {
            invalidateSession(request, response);
            return false;
        }

        String storedUserAgent = (String) session.getAttribute(SESSION_USER_AGENT_ATTR);
        String currentUserAgent = request.getHeader("User-Agent");
        if (storedUserAgent == null || !storedUserAgent.equals(currentUserAgent)) {
            invalidateSession(request, response);
            return false;
        }

        session.setAttribute(SESSION_LAST_ACTIVITY_ATTR, System.currentTimeMillis());

        Long creationTime = (Long) session.getAttribute(SESSION_CREATION_TIME_ATTR);
        if (creationTime != null && (System.currentTimeMillis() - creationTime) > AppConfig.SESSION_ROTATION_MS) {
            rotateSession(request, response);
        }

        return true;
    }

    public static void rotateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession oldSession = request.getSession(false);
        if (oldSession == null) {
            return;
        }

        String email = (String) oldSession.getAttribute("email");
        String storedIP = (String) oldSession.getAttribute(SESSION_IP_ATTR);
        String storedUserAgent = (String) oldSession.getAttribute(SESSION_USER_AGENT_ATTR);

        oldSession.invalidate();

        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(SESSION_TOKEN_ATTR, generateSecureToken());
        newSession.setAttribute(SESSION_IP_ATTR, storedIP);
        newSession.setAttribute(SESSION_USER_AGENT_ATTR, storedUserAgent);

        long currentTime = System.currentTimeMillis();
        newSession.setAttribute(SESSION_CREATION_TIME_ATTR, currentTime);
        newSession.setAttribute(SESSION_LAST_ACTIVITY_ATTR, currentTime);
        newSession.setAttribute("email", email);
    }

    public static void invalidateSession(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionId = session.getId();
            String email = (String) session.getAttribute("email");
            session.invalidate();
            logger.log(Level.INFO, "Invalidated session for user: " + email + " with session ID: " + sessionId);
        }
    }

    private static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getEncoder().encodeToString(tokenBytes);
    }
}
