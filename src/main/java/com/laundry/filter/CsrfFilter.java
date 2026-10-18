package com.laundry.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class CsrfFilter implements Filter {
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(true);
        String token = (String) session.getAttribute("csrfToken");
        if (token == null) {
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute("csrfToken", token);
        }
        if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
            String submitted = httpRequest.getParameter("csrfToken");
            if (submitted == null || !constantTimeEquals(token, submitted)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Your form session expired. Refresh the page and try again.");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) return false;
        int result = 0;
        for (int i = 0; i < expected.length(); i++) result |= expected.charAt(i) ^ actual.charAt(i);
        return result == 0;
    }
}
