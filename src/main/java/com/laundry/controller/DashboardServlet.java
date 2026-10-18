package com.laundry.controller;

import com.laundry.service.DeliveryService;
import com.laundry.service.FeedbackService;
import com.laundry.service.OrderService;
import com.laundry.service.ReportService;
import com.laundry.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final ReportService reportService = new ReportService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final OrderService orderService = new OrderService();
    private final DeliveryService deliveryService = new DeliveryService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Long customerId = WebUtil.currentCustomerId(request);
            if (hasRole(request, "CUSTOMER")) {
                request.setAttribute("orders", orderService.list("", "", "", "", customerId));
                request.setAttribute("deliveries", deliveryService.list("", "", customerId));
            } else {
                request.setAttribute("stats", reportService.dashboard());
            }
            request.setAttribute("recentFeedback", feedbackService.recent(5));
            view(request, response, "common/dashboard");
        } catch (Exception e) {
            failure(request, response, e, "/login");
        }
    }
}
