package com.lottery.oauth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.lottery.db.UserDAO;
import com.lottery.model.User;
import com.lottery.util.OAuthConfig;
import com.lottery.util.InputValidator;
import com.lottery.util.PasswordUtil;
import com.lottery.util.SessionManager;

public abstract class BaseOAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final Logger logger = Logger.getLogger(BaseOAuthServlet.class.getName());

    protected OAuthConfig oauthConfig;

    public BaseOAuthServlet() {
        oauthConfig = OAuthConfig.getInstance();
    }

    protected abstract String getProviderName();

    protected abstract String getAuthorizationUrl();

    protected abstract String getTokenUrl();

    protected abstract String buildTokenRequestBody(String code) throws IOException;

    protected abstract String getUserInfoUrl(String accessToken);

    protected abstract String[] getUserInfoKeys(); 
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String code = request.getParameter("code");

        logger.log(Level.INFO, "Received OAuth callback for provider: " + getProviderName());

        if (code != null && InputValidator.containsSQLInjectionPatterns(code)) {
            request.setAttribute("errorMessage", "Invalid request.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        if (code == null || code.isEmpty()) {
            response.sendRedirect(getAuthorizationUrl());
            return;
        }

        try {
            
            String accessToken = exchangeCodeForAccessToken(code);

            if (accessToken == null || accessToken.isEmpty()) {
                request.setAttribute("errorMessage", "Failed to get access token from " + getProviderName());
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            Map<String, String> userInfo = getUserInfo(accessToken);

            if (userInfo == null || userInfo.isEmpty()) {
                request.setAttribute("errorMessage", "Failed to get user info from " + getProviderName());
                request.getRequestDispatcher("/login.jsp").forward(request, response);
                return;
            }

            User user = findOrCreateUser(userInfo);

            setupSession(request, response, user);

            if (!user.isPasswordSet()) {
                response.sendRedirect(request.getContextPath() + "/setPassword");
            } else {
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/userManagement");
                } else {
                    response.sendRedirect(request.getContextPath() + "/userLottery");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", getProviderName() + " authentication failed: " + e.getMessage());
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    protected String exchangeCodeForAccessToken(String code) throws IOException {
        URL url = new URL(getTokenUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);

        String postData = buildTokenRequestBody(code);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String responseStr = response.toString();
            return extractValueFromJson(responseStr, "access_token");
        } else {
            
            logger.log(Level.SEVERE, "OAuth Token Exchange Failed for " + getProviderName());
            logger.log(Level.SEVERE, "Response Code: " + responseCode);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                logger.log(Level.SEVERE, "Error Response: " + errorResponse.toString());
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Could not read error response: " + e.getMessage());
            }
        }

        return null;
    }

    protected Map<String, String> getUserInfo(String accessToken) throws IOException {
        URL url = new URL(getUserInfoUrl(accessToken));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            return parseUserInfoJson(response.toString(), getUserInfoKeys());
        }

        return null;
    }

    protected User findOrCreateUser(Map<String, String> userInfo) throws Exception {
        String email = userInfo.get("email");
        String firstName = userInfo.get("firstName");
        String lastName = userInfo.get("lastName");

        if (email != null && !InputValidator.isValidEmail(email)) {
            throw new Exception("Invalid email from " + getProviderName());
        }

        if (firstName != null)
            firstName = InputValidator.sanitizeString(firstName);
        if (lastName != null)
            lastName = InputValidator.sanitizeString(lastName);

        if ((firstName != null && InputValidator.containsSQLInjectionPatterns(firstName)) ||
                (lastName != null && InputValidator.containsSQLInjectionPatterns(lastName))) {
            throw new Exception("Invalid user data from " + getProviderName());
        }

        User user = UserDAO.getUserByEmail(email);

        if (user == null) {
            
            String password = UUID.randomUUID().toString(); 
            String hashedPassword = PasswordUtil.hash(password);

            user = new User(email, hashedPassword);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setActive(true);
            user.setPasswordSet(false); 

            UserDAO.createUser(user);
        }

        return user;
    }

    protected void setupSession(HttpServletRequest request, HttpServletResponse response, User user) {
        
        HttpSession session = SessionManager.createSecureSession(request, response,
                user.getEmail());

        session.setAttribute("user", user);

        session.setAttribute("oauthProvider", getProviderName());

        if (!user.isPasswordSet()) {
            session.setAttribute("needsPasswordSetup", true);
        } else {
            session.setAttribute("needsPasswordSetup", false);
        }

        String displayName = (user.getFirstName() != null ? user.getFirstName() : "") +
                " " + (user.getLastName() != null ? user.getLastName() : "");
        displayName = displayName.trim();
        if (displayName.isEmpty()) {
            displayName = user.getEmail();
        }
        session.setAttribute("displayName", displayName);

        UserDAO.updateLastLoginDate(user.getEmail());
    }

    protected String extractValueFromJson(String json, String key) {
        
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex != -1) {
            
            int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
            if (colonIndex != -1) {
                
                int openQuote = json.indexOf("\"", colonIndex + 1);
                if (openQuote != -1) {
                    int closeQuote = json.indexOf("\"", openQuote + 1);
                    if (closeQuote != -1) {
                        return json.substring(openQuote + 1, closeQuote);
                    }
                }
            }
        }
        return null;
    }

    protected Map<String, String> parseUserInfoJson(String json, String[] keys) {
        Map<String, String> userInfo = new HashMap<>();
        
        String[] standardKeys = { "email", "firstName", "lastName" };

        for (int i = 0; i < keys.length; i++) {
            if (i >= standardKeys.length)
                break;

            String jsonKey = keys[i];
            if (jsonKey != null) {
                String value = extractValueFromJson(json, jsonKey);
                if (value != null) {
                    userInfo.put(standardKeys[i], value);
                }
            }
        }

        return userInfo;
    }
}
