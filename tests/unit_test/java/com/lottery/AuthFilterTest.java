package com.lottery;

import com.lottery.util.SessionManager;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("4. AuthFilter (/*)")
class AuthFilterTest {

    private AuthFilter filter;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new AuthFilter();
    }

    private void setupPublicUrl(String uri) {
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getContextPath()).thenReturn("/ctx");
    }

    @Test
    @DisplayName("#1 doFilter - Public URL (login.jsp)")
    void testDoFilter_publicUrl_loginJsp() throws Exception {
        setupPublicUrl("/ctx/login.jsp");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#2 doFilter - Public URL (register)")
    void testDoFilter_publicUrl_register() throws Exception {
        setupPublicUrl("/ctx/register");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#3 doFilter - Public URL (assets/css)")
    void testDoFilter_publicUrl_assetsCss() throws Exception {
        setupPublicUrl("/ctx/assets/style.css");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#4 doFilter - Public URL (homepage)")
    void testDoFilter_publicUrl_homepage() throws Exception {
        setupPublicUrl("/ctx/homepage");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#5 doFilter - Public URL (OAuth callback)")
    void testDoFilter_publicUrl_oauthCallback() throws Exception {
        setupPublicUrl("/ctx/oauth/google/callback");

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#6 doFilter - Public URL (static files)")
    void testDoFilter_publicUrl_staticFiles() throws Exception {
        
        setupPublicUrl("/ctx/assets/script.js");
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    @DisplayName("#7 doFilter - Protected URL with valid session")
    void testDoFilter_protectedUrl_validSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/ctx/userLottery");
        when(request.getContextPath()).thenReturn("/ctx");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.validateSession(request, response)).thenReturn(true);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("#8 doFilter - Protected URL with invalid session")
    void testDoFilter_protectedUrl_invalidSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/ctx/userLottery");
        when(request.getContextPath()).thenReturn("/ctx");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.validateSession(request, response)).thenReturn(false);

            filter.doFilter(request, response, chain);

            verify(response).sendRedirect("/ctx/login.jsp");
            verify(chain, never()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("#9 doFilter - Protected URL with expired session")
    void testDoFilter_protectedUrl_expiredSession() throws Exception {
        when(request.getRequestURI()).thenReturn("/ctx/userLottery");
        when(request.getContextPath()).thenReturn("/ctx");

        try (MockedStatic<SessionManager> sm = mockStatic(SessionManager.class)) {
            sm.when(() -> SessionManager.validateSession(request, response)).thenReturn(false);

            filter.doFilter(request, response, chain);

            verify(response).sendRedirect("/ctx/login.jsp");
        }
    }
}
