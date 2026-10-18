package com.laundry.controller;

import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

public abstract class BaseServlet extends HttpServlet {
    protected void view(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + jsp + ".jsp").forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void failure(HttpServletRequest request, HttpServletResponse response, Exception exception, String redirectPath) throws IOException {
        String message;
        if (exception instanceof IllegalArgumentException || exception instanceof SecurityException) {
            message = exception.getMessage();
        } else if (exception instanceof SQLException sqlException && "23000".equals(sqlException.getSQLState())) {
            message = "That value is already in use or the selected record is still referenced by other data.";
        } else {
            getServletContext().log("Request failed", exception);
            message = "The request could not be completed. Check the data and database connection, then try again.";
        }
        WebUtil.flash(request, "danger", message);
        redirect(request, response, redirectPath);
    }

    protected boolean hasRole(HttpServletRequest request, String... roles) {
        Object current = request.getSession().getAttribute("role");
        if (current == null) return false;
        for (String role : roles) if (role.equals(current.toString())) return true;
        return false;
    }
}
