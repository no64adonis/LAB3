package com.lottery;

import com.lottery.util.OAuthConfig;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("11. GoogleOAuthServlet (/oauth/google/callback)")
class GoogleOAuthServletTest {

    private GoogleOAuthServlet servlet;

    @Mock private OAuthConfig oauthConfig;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new GoogleOAuthServlet();
        
        Field configField = servlet.getClass().getSuperclass().getDeclaredField("oauthConfig");
        configField.setAccessible(true);
        configField.set(servlet, oauthConfig);
    }

    @Test
    @DisplayName("#1 getProviderName returns Google")
    void testGetProviderName() {
        assertEquals("Google", servlet.getProviderName());
    }

    @Test
    @DisplayName("#2 getTokenUrl returns Google token endpoint")
    void testGetTokenUrl() {
        assertEquals("https://oauth2.googleapis.com/token", servlet.getTokenUrl());
    }

    @Test
    @DisplayName("#3 getUserInfoUrl returns correct URL")
    void testGetUserInfoUrl() {
        String url = servlet.getUserInfoUrl("test_token");
        assertEquals("https://www.googleapis.com/oauth2/v2/userinfo?access_token=test_token", url);
    }

    @Test
    @DisplayName("#4 getUserInfoKeys returns email, given_name, family_name")
    void testGetUserInfoKeys() {
        String[] keys = servlet.getUserInfoKeys();
        assertArrayEquals(new String[]{"email", "given_name", "family_name"}, keys);
    }

    @Test
    @DisplayName("#5 getAuthorizationUrl constructs valid URL")
    void testGetAuthorizationUrl() {
        when(oauthConfig.getGoogleClientId()).thenReturn("test_client_id");
        when(oauthConfig.getGoogleRedirectUri()).thenReturn("http://localhost:8080/oauth/google/callback");

        String url = servlet.getAuthorizationUrl();

        assertTrue(url.contains("accounts.google.com"));
        assertTrue(url.contains("test_client_id"));
    }

    @Test
    @DisplayName("#6 buildTokenRequestBody includes all params")
    void testBuildTokenRequestBody() throws Exception {
        when(oauthConfig.getGoogleClientId()).thenReturn("client_id");
        when(oauthConfig.getGoogleClientSecret()).thenReturn("client_secret");
        when(oauthConfig.getGoogleRedirectUri()).thenReturn("http://localhost/callback");

        String body = servlet.buildTokenRequestBody("auth_code");

        assertTrue(body.contains("code=auth_code"));
        assertTrue(body.contains("grant_type=authorization_code"));
    }

    @Test
    @DisplayName("#7 getAuthorizationUrl includes scope")
    void testGetAuthorizationUrl_scope() {
        when(oauthConfig.getGoogleClientId()).thenReturn("id");
        when(oauthConfig.getGoogleRedirectUri()).thenReturn("http://localhost/cb");

        String url = servlet.getAuthorizationUrl();

        assertTrue(url.contains("scope="));
    }

    @Test
    @DisplayName("#8 getAuthorizationUrl includes response_type=code")
    void testGetAuthorizationUrl_responseType() {
        when(oauthConfig.getGoogleClientId()).thenReturn("id");
        when(oauthConfig.getGoogleRedirectUri()).thenReturn("http://localhost/cb");

        String url = servlet.getAuthorizationUrl();

        assertTrue(url.contains("response_type=code"));
    }
}
