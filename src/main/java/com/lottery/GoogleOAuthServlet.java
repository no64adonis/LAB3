package com.lottery;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.annotation.WebServlet;

import com.lottery.oauth.BaseOAuthServlet;
public class GoogleOAuthServlet extends BaseOAuthServlet {
    private static final long serialVersionUID = 1L;
    protected String getProviderName() {
        return "Google";
    }
    protected String getAuthorizationUrl() {
        try {
            return "https://accounts.google.com/o/oauth2/auth?" +
                    "client_id=" + oauthConfig.getGoogleClientId() +
                    "&redirect_uri="
                    + URLEncoder.encode(oauthConfig.getGoogleRedirectUri(), StandardCharsets.UTF_8.toString()) +
                    "&scope=" + URLEncoder.encode("email profile", StandardCharsets.UTF_8.toString()) +
                    "&response_type=code" +
                    "&access_type=online";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    protected String getTokenUrl() {
        return "https://oauth2.googleapis.com/token";
    }
    protected String buildTokenRequestBody(String code) throws IOException {
        return "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8.toString()) +
                "&client_id=" + URLEncoder.encode(oauthConfig.getGoogleClientId(), StandardCharsets.UTF_8.toString()) +
                "&client_secret="
                + URLEncoder.encode(oauthConfig.getGoogleClientSecret(), StandardCharsets.UTF_8.toString()) +
                "&redirect_uri="
                + URLEncoder.encode(oauthConfig.getGoogleRedirectUri(), StandardCharsets.UTF_8.toString()) +
                "&grant_type=authorization_code";
    }
    protected String getUserInfoUrl(String accessToken) {
        return "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
    }
    protected String[] getUserInfoKeys() {
        return new String[] { "email", "given_name", "family_name" };
    }
}