package com.lottery.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OAuthConfig {
    public static final String GOOGLE_CLIENT_ID = "983186456814-h0gbid9h86576smkg6k89h4ll5jj8vae.apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = "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";
    public static final String GOOGLE_REDIRECT_URI = "http://localhost:8080/Lottery/oauth/google/callback";
    private static OAuthConfig instance;
    private Properties properties;

    private OAuthConfig() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("oauth.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized OAuthConfig getInstance() {
        if (instance == null) {
            instance = new OAuthConfig();
        }
        return instance;
    }

    public String getGoogleClientId() {
        return properties.getProperty("google.client.id", GOOGLE_CLIENT_ID);
    }

    public String getGoogleClientSecret() {
        return properties.getProperty("google.client.secret", GOOGLE_CLIENT_SECRET);
    }

    public String getGoogleRedirectUri() {
        return properties.getProperty("google.redirect.uri", GOOGLE_REDIRECT_URI);
    }

}