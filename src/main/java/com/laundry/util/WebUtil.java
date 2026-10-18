package com.laundry.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class WebUtil {
    private WebUtil() { }

    public static void flash(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("flashType", type);
        request.getSession().setAttribute("flashMessage", message);
    }

    public static Long currentCustomerId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("customerId") == null) return null;
        return (Long) session.getAttribute("customerId");
    }

    public static String path(HttpServletRequest request) {
        String path = request.getPathInfo();
        return path == null || path.equals("/") ? "/" : path;
    }

    public static long longParameter(HttpServletRequest request, String name) {
        try {
            return Long.parseLong(request.getParameter(name));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + name + ".");
        }
    }
}
