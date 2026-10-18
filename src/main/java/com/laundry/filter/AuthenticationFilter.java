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

public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = PathUtil.applicationPath(httpRequest);
        boolean publicPath = path.equals("/") || path.equals("/login") || path.equals("/signup")
                || path.equals("/register") || path.equals("/index.jsp") || path.equals("/home") || path.equals("/track")
                || path.startsWith("/assets/") || path.startsWith("/favicon");
        HttpSession session = httpRequest.getSession(false);
        if (!publicPath && (session == null || session.getAttribute("userId") == null)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?expired=1");
            return;
        }
        if (!publicPath) {
            try {
                com.laundry.model.User user = new com.laundry.dao.UserDAO().findById(((Number)session.getAttribute("userId")).longValue()).orElse(null);
                if (user == null || !"ACTIVE".equals(user.getStatus())
                        || !user.getRole().name().equals(session.getAttribute("role"))
                        || !user.getPasswordHash().equals(session.getAttribute("credentialSnapshot"))) {
                    session.invalidate();
                    httpResponse.sendRedirect(httpRequest.getContextPath()+"/login?expired=1");
                    return;
                }
                session.setAttribute("userName",user.getFullName());
                session.setAttribute("userEmail",user.getEmail());
                session.setAttribute("customerId",user.getCustomerId());
            } catch (java.sql.SQLException e) {
                httpRequest.getServletContext().log("Session verification failed",e);
                httpResponse.sendError(503,"Account verification is temporarily unavailable. Please try again.");
                return;
            }
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
        }
        chain.doFilter(request, response);
    }
}
