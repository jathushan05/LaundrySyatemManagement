package com.laundry.controller;

import com.laundry.model.Customer;
import com.laundry.service.CustomerService;
import com.laundry.service.OrderService;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/customers/*")
public class CustomerServlet extends BaseServlet {
    private final CustomerService customerService = new CustomerService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = WebUtil.path(request);
        try {
            if ("/profile".equals(path)) {
                Long customerId = WebUtil.currentCustomerId(request);
                if (customerId == null) throw new SecurityException("A customer profile is not connected to this account.");
                showCustomer(request, response, customerId, true);
            } else if ("/new".equals(path)) {
                request.setAttribute("customer", new Customer());
                view(request, response, "customer/form");
            } else if ("/edit".equals(path)) {
                request.setAttribute("customer", customerService.get(WebUtil.longParameter(request, "id")));
                view(request, response, "customer/form");
            } else if ("/view".equals(path)) {
                showCustomer(request, response, WebUtil.longParameter(request, "id"), false);
            } else {
                request.setAttribute("customers", customerService.list(request.getParameter("search"), request.getParameter("status")));
                view(request, response, "customer/list");
            }
        } catch (Exception e) {
            failure(request, response, e, "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (hasRole(request, "CUSTOMER", "LAUNDRY_STAFF", "INVENTORY_MANAGER", "DELIVERY_COORDINATOR")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN); return;
        }
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                Customer customer = readCustomer(request, false);
                customerService.create(customer, request.getParameter("password"));
                WebUtil.flash(request, "success", "Customer registered successfully.");
            } else if ("update".equals(action)) {
                customerService.update(readCustomer(request, true));
                WebUtil.flash(request, "success", "Customer details updated.");
            } else if ("deactivate".equals(action)) {
                customerService.deactivate(WebUtil.longParameter(request, "id"));
                WebUtil.flash(request, "success", "Customer account deactivated.");
            } else throw new IllegalArgumentException("Invalid customer action.");
            redirect(request, response, "/customers/");
        } catch (Exception e) {
            failure(request, response, e, "/customers/");
        }
    }

    private void showCustomer(HttpServletRequest request, HttpServletResponse response, long id, boolean profile) throws Exception {
        request.setAttribute("customer", customerService.get(id));
        request.setAttribute("orders", orderService.list("", "", "", "", id));
        request.setAttribute("profileMode", profile);
        view(request, response, "customer/view");
    }

    private Customer readCustomer(HttpServletRequest request, boolean update) {
        Customer customer = new Customer();
        if (update) customer.setId(WebUtil.longParameter(request, "id"));
        customer.setFullName(request.getParameter("fullName"));
        customer.setEmail(request.getParameter("email"));
        customer.setPhone(request.getParameter("phone"));
        customer.setAddress(request.getParameter("address"));
        customer.setAccountStatus(update ? request.getParameter("accountStatus") : "ACTIVE");
        return customer;
    }
}
