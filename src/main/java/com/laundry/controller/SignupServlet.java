package com.laundry.controller;

import com.laundry.dao.UserDAO;
import com.laundry.model.Customer;
import com.laundry.service.CustomerService;
import com.laundry.util.ValidationUtil;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet({"/signup", "/register"})
public class SignupServlet extends BaseServlet {
    private final CustomerService customerService = new CustomerService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            redirect(request, response, "/dashboard");
            return;
        }
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        view(request, response, "auth/signup");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("address", address);

        if (password == null || password.length() < 8) {
            request.setAttribute("error", "Password must contain at least 8 characters.");
            view(request, response, "auth/signup");
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match.");
            view(request, response, "auth/signup");
            return;
        }

        List<String> validationErrors = ValidationUtil.customer(fullName, email, phone, address);
        if (!validationErrors.isEmpty()) {
            request.setAttribute("error", String.join(" ", validationErrors));
            view(request, response, "auth/signup");
            return;
        }

        try {
            if (userDAO.findByEmail(email).isPresent()) {
                request.setAttribute("error", "An account with this email address already exists. Please sign in instead.");
                view(request, response, "auth/signup");
                return;
            }

            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setEmail(email);
            customer.setPhone(phone);
            customer.setAddress(address);
            customer.setAccountStatus("ACTIVE");

            Customer createdCustomer = customerService.create(customer, password);

            HttpSession session = request.getSession(true);
            session.setAttribute("registeredEmail", createdCustomer.getEmail());
            WebUtil.flash(request, "success",
                    "Your AquaClean account was created successfully. Sign in with your new email and password.");
            redirect(request, response, "/login");
        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            view(request, response, "auth/signup");
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState())) {
                request.setAttribute("error", "An account with this email or phone number is already registered.");
            } else {
                getServletContext().log("Customer registration failed", e);
                request.setAttribute("error", "Unable to complete registration. Please check the database connection and try again.");
            }
            view(request, response, "auth/signup");
        }
    }
}
