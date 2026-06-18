package com.lottery;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.util.SessionManager;
public class AuthFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
    }
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        if (requestURI.equals(contextPath + "/") ||
                requestURI.equals(contextPath) ||
                requestURI.endsWith("/index.jsp") ||
                requestURI.endsWith("/login.jsp") ||
                requestURI.endsWith("/login") ||
                requestURI.endsWith("/register") ||
                requestURI.endsWith("/register.jsp") ||
                requestURI.endsWith("/error/404.jsp") ||
                requestURI.endsWith("/error/500.jsp") ||
                requestURI.endsWith("/homepage") ||
                requestURI.contains("/oauth/google/callback") ||
                requestURI.endsWith("/forgotPassword.jsp") ||
                requestURI.endsWith("/passwordReset") ||
                requestURI.endsWith("/userResetPassword.jsp") ||
                requestURI.endsWith("/setPassword") ||
                requestURI.contains("/assets/") ||
                requestURI.endsWith(".css") ||
                requestURI.endsWith(".js") ||
                requestURI.endsWith(".png") ||
                requestURI.endsWith(".jpg") ||
                requestURI.endsWith(".jpeg") ||
                requestURI.endsWith(".gif") ||
                requestURI.endsWith(".ico") ||
                requestURI.endsWith(".svg")) {
            chain.doFilter(request, response);
            return;
        }

        if (SessionManager.validateSession(httpRequest, httpResponse)) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.jsp");
        }
    }
    public void destroy() {
    }
}
