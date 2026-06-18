package com.lottery.taglib;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;

public final class PaginationUrlBuilder {

    private PaginationUrlBuilder() {
        
    }

    public static String buildPaginationUrl(HttpServletRequest request, String baseAction, int pageNumber) {
        StringBuilder url = new StringBuilder(baseAction).append("?");

        try {
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                
                if (!"page".equals(name)) {
                    String[] values = request.getParameterValues(name);
                    for (String value : values) {
                        if (value != null && !value.isEmpty()) {
                            url.append(URLEncoder.encode(name, "UTF-8"))
                                    .append("=")
                                    .append(URLEncoder.encode(value, "UTF-8"))
                                    .append("&");
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        url.append("page=").append(pageNumber);

        return url.toString();
    }

    public static String buildBaseUrl(HttpServletRequest request, String baseAction) {
        StringBuilder url = new StringBuilder(baseAction).append("?");

        try {
            Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String name = paramNames.nextElement();
                if (!"page".equals(name)) {
                    String[] values = request.getParameterValues(name);
                    for (String value : values) {
                        if (value != null && !value.isEmpty()) {
                            url.append(URLEncoder.encode(name, "UTF-8"))
                                    .append("=")
                                    .append(URLEncoder.encode(value, "UTF-8"))
                                    .append("&");
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }

        return url.toString();
    }
}
