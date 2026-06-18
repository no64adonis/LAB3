package com.lottery.oauth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("12. BaseOAuthServlet (abstract)")
class BaseOAuthServletTest {

    private TestOAuthServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new TestOAuthServlet();
    }

    @Test
    @DisplayName("#1 extractValueFromJson - valid key")
    void testExtractValueFromJson_valid() {
        String json = "{\"access_token\":\"abc123\",\"token_type\":\"Bearer\"}";
        assertEquals("abc123", servlet.extractValueFromJson(json, "access_token"));
    }

    @Test
    @DisplayName("#2 extractValueFromJson - missing key")
    void testExtractValueFromJson_missing() {
        String json = "{\"other_key\":\"value\"}";
        assertNull(servlet.extractValueFromJson(json, "access_token"));
    }

    @Test
    @DisplayName("#3 extractValueFromJson - with spaces")
    void testExtractValueFromJson_withSpaces() {
        String json = "{\"access_token\" : \"abc123\"}";
        assertEquals("abc123", servlet.extractValueFromJson(json, "access_token"));
    }

    @Test
    @DisplayName("#4 parseUserInfoJson - all keys present")
    void testParseUserInfoJson_allKeys() {
        String json = "{\"email\":\"user@test.com\",\"given_name\":\"John\",\"family_name\":\"Doe\"}";
        String[] keys = {"email", "given_name", "family_name"};

        Map<String, String> result = servlet.parseUserInfoJson(json, keys);

        assertEquals("user@test.com", result.get("email"));
        assertEquals("John", result.get("firstName"));
        assertEquals("Doe", result.get("lastName"));
    }

    @Test
    @DisplayName("#5 parseUserInfoJson - missing lastName")
    void testParseUserInfoJson_missingLastName() {
        String json = "{\"email\":\"user@test.com\",\"given_name\":\"John\"}";
        String[] keys = {"email", "given_name", "family_name"};

        Map<String, String> result = servlet.parseUserInfoJson(json, keys);

        assertEquals("user@test.com", result.get("email"));
        assertEquals("John", result.get("firstName"));
        assertNull(result.get("lastName"));
    }

    @Test
    @DisplayName("#6 parseUserInfoJson - empty json")
    void testParseUserInfoJson_emptyJson() {
        String json = "{}";
        String[] keys = {"email", "given_name", "family_name"};

        Map<String, String> result = servlet.parseUserInfoJson(json, keys);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("#7 extractValueFromJson - empty string")
    void testExtractValueFromJson_emptyString() {
        String json = "{\"key\":\"\"}";
        assertEquals("", servlet.extractValueFromJson(json, "key"));
    }

    @Test
    @DisplayName("#8 extractValueFromJson - nested json")
    void testExtractValueFromJson_nested() {
        String json = "{\"outer\":\"value1\",\"inner\":{\"key\":\"value2\"}}";
        assertEquals("value1", servlet.extractValueFromJson(json, "outer"));
    }

    @Test
    @DisplayName("#9 extractValueFromJson - special characters in value")
    void testExtractValueFromJson_specialChars() {
        String json = "{\"name\":\"John O\"}";
        assertEquals("John O", servlet.extractValueFromJson(json, "name"));
    }

    @Test
    @DisplayName("#10 parseUserInfoJson - maps provider keys to standard keys")
    void testParseUserInfoJson_keyMapping() {
        
        String json = "{\"email\":\"fb@test.com\",\"first_name\":\"FB\",\"last_name\":\"User\"}";
        String[] keys = {"email", "first_name", "last_name"};

        Map<String, String> result = servlet.parseUserInfoJson(json, keys);

        assertEquals("fb@test.com", result.get("email"));
        assertEquals("FB", result.get("firstName"));
        assertEquals("User", result.get("lastName"));
    }

    private static class TestOAuthServlet extends BaseOAuthServlet {
        @Override
        protected String getProviderName() { return "Test"; }
        @Override
        protected String getAuthorizationUrl() { return "http://test.com/auth"; }
        @Override
        protected String getTokenUrl() { return "http://test.com/token"; }
        @Override
        protected String buildTokenRequestBody(String code) { return "code=" + code; }
        @Override
        protected String getUserInfoUrl(String accessToken) { return "http://test.com/userinfo"; }
        @Override
        protected String[] getUserInfoKeys() { return new String[]{"email", "name", "surname"}; }
    }
}
