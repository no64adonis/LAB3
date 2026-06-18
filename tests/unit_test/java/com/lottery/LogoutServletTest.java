package com.lottery;

import com.lottery.util.SessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("2. LogoutServlet (/logout)")
class LogoutServletTest {

    private LogoutServlet servlet;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        servlet = new LogoutServlet();
    }

    @Test
    @DisplayName("#1 GET - Successful logout with active session")
    void testDoGet_successfulLogout() throws Exception {
        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            servlet.doGet(request, response);

            sm.verify(() -> SessionManager.invalidateSession(request, response));
            verify(response).sendRedirect("index.jsp");
        }
    }

    @Test
    @DisplayName("#2 GET - Logout with no active session")
    void testDoGet_noActiveSession() throws Exception {
        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            servlet.doGet(request, response);

            sm.verify(() -> SessionManager.invalidateSession(request, response));
            verify(response).sendRedirect("index.jsp");
        }
    }
}
