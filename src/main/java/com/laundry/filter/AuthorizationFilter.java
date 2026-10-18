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
import java.util.Set;

public class AuthorizationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = PathUtil.applicationPath(httpRequest);
        if (path.equals("/") || path.equals("/login") || path.equals("/signup") || path.equals("/register")
                || path.equals("/index.jsp") || path.equals("/logout") || path.equals("/home") || path.equals("/track")
                || path.startsWith("/assets/")) {
            chain.doFilter(request, response);
            return;
        }
        HttpSession session = httpRequest.getSession(false);
        String role = session == null ? null : (String) session.getAttribute("role");
        if (role == null || !allowed(role, path)) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean allowed(String role, String path) {
        if ("ADMINISTRATOR".equals(role)) return true;
        if (path.equals("/dashboard")) return true;
        return switch (role) {
            case "RECEPTIONIST" -> startsWithAny(path, "/customers", "/orders");
            case "LAUNDRY_STAFF" -> path.startsWith("/orders");
            case "INVENTORY_MANAGER" -> path.startsWith("/inventory");
            case "DELIVERY_COORDINATOR" -> path.startsWith("/deliveries");
            case "CUSTOMER" -> path.startsWith("/orders") || path.startsWith("/deliveries")
                    || path.startsWith("/feedback") || path.equals("/customers/profile");
            default -> false;
        };
    }

    private boolean startsWithAny(String path, String... prefixes) {
        return Set.of(prefixes).stream().anyMatch(path::startsWith);
    }
}
